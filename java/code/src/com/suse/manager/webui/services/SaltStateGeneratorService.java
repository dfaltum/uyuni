/**
 * Copyright (c) 2016 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.services;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.MinionServerUtils;
import com.suse.manager.webui.utils.RepoFileUtils;
import com.suse.manager.webui.utils.SaltCustomState;
import com.suse.manager.webui.utils.SaltPillar;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.suse.manager.webui.utils.SaltFileUtils.defaultExtension;

/**
 * Service to manage the Salt states generated by Suse Manager.
 */
public enum SaltStateGeneratorService {

    // Singleton instance of this class
    INSTANCE;

    /** Logger */
    private static final Logger LOG = Logger.getLogger(SaltStateGeneratorService.class);

    public static final String SALT_CUSTOM_STATES = "custom";

    public static final String GENERATED_PILLAR_ROOT = "/srv/susemanager/pillar";

    /**
     * Generate server specific pillar if the given server is a minion.
     * @param server the minion server
     */
    public void generatePillarForServer(Server server) {
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        LOG.debug("Generating pillar file for server name= " + server.getName() +
                " digitalId=" + server.getDigitalServerId());

        List<ManagedServerGroup> groups = ServerGroupFactory.listManagedGroups(server);
        List<Long> groupIds = groups.stream()
                .map(g -> g.getId()).collect(Collectors.toList());
        SaltPillar pillar = new SaltPillar();
        pillar.add("org_id", server.getOrg().getId());
        pillar.add("group_id", groupIds.toArray(new Long[groupIds.size()]));

        try {
            Path baseDir = Paths.get(GENERATED_PILLAR_ROOT);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(
                    defaultExtension("server_" + server.getDigitalServerId()));
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(pillar);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Remove the corresponding pillar data if the server is a minion.
     * @param server the minion server
     */
    public void removePillarForServer(Server server) {
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        LOG.debug("Removing pillar file for server name= " + server.getName() +
                " digitalId=" + server.getDigitalServerId());
        Path baseDir = Paths.get(GENERATED_PILLAR_ROOT);
        Path filePath = baseDir.resolve(
                defaultExtension("server_" + server.getDigitalServerId()));
        try {
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            LOG.error("Could not remove pillar file " + filePath);
        }
    }

    /**
     * Remove the custom states assignments for minion server.
     * @param server the minion server
     */
    public void removeCustomStateAssignments(Server server) {
        removeCustomStateAssignments("custom_" + server.getDigitalServerId());
    }

    /**
     * Remove the custom states assignments for server group.
     * @param group the server group
     */
    public void removeCustomStateAssignments(ServerGroup group) {
        removeCustomStateAssignments("group_" + group.getId());
    }

    /**
     * Remove the custom states assignments for an organization.
     * @param org the organization
     */
    public void removeCustomStateAssignments(Org org) {
        removeCustomStateAssignments("org_" + org.getId());
    }

    private void removeCustomStateAssignments(String file) {
        Path baseDir = Paths.get(
                RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
        Path filePath = baseDir.resolve(defaultExtension(file));

        try {
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate .sls file to assign custom states to a server.
     * @param serverStateRevision the state revision of a server
     */
    public void generateServerCustomState(ServerStateRevision serverStateRevision) {
        Server server = serverStateRevision.getServer();
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        LOG.debug("Generating custom state SLS file for server: " + server.getId());

        generateCustomStates(server.getOrg().getId(), serverStateRevision,
                "custom_" + server.getDigitalServerId());
    }

    /**
     * Generate .sls file to assign custom states to a server group.
     * @param groupStateRevision the state revision of a server group
     */
    public void generateGroupCustomState(ServerGroupStateRevision groupStateRevision) {
        ServerGroup group = groupStateRevision.getGroup();
        LOG.debug("Generating custom state SLS file for server group: " + group.getId());

        generateCustomStates(group.getOrg().getId(), groupStateRevision,
                "group_" + group.getId());
    }

    /**
     * Generate .sls file to assign custom states to an org.
     * @param orgStateRevision the state revision of an org
     */

    public void generateOrgCustomState(OrgStateRevision orgStateRevision) {
        Org org = orgStateRevision.getOrg();
        LOG.debug("Generating custom state SLS file for organization: " + org.getId());

        generateCustomStates(org.getId(), orgStateRevision,
                "org_" + org.getId());
    }

    private void generateCustomStates(long orgId, StateRevision stateRevision,
                                      String fileName) {
        Set<String> stateNames = stateRevision.getCustomStates()
                .stream()
                .filter(s-> !s.isDeleted()) // skip deleted states
                .map(s -> s.getStateName())
                .collect(Collectors.toSet());

        generateCustomStateAssignmentFile(orgId, fileName, stateNames);
    }

    private void generateCustomStateAssignmentFile(long orgId, String fileName,
        Set<String> stateNames) {
        stateNames = SaltAPIService.INSTANCE.resolveOrgStates(
                orgId, stateNames);

        Path baseDir = Paths.get(
                RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
        try {
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(defaultExtension(fileName));
            com.suse.manager.webui.utils.SaltStateGenerator saltStateGenerator =
                    new com.suse.manager.webui.utils.SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltCustomState(stateNames));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate pillar and custom states assignments for a
     * newly registered server.
     * @param server newly registered server
     */
    public void registerServer(Server server) {
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        // TODO create an empty revision ?
        generatePillarForServer(server);
        generateCustomStateAssignmentFile(server.getOrg().getId(),
                "custom_" + server.getDigitalServerId(),
                Collections.emptySet());
    }

    /**
     * Remove pillars and custom states assignments of a server.
     * @param server the server
     */
    public void removeServer(Server server) {
        if (!MinionServerUtils.isMinionServer(server)) {
            return;
        }
        removePillarForServer(server);
        removeCustomStateAssignments(server);
    }

    /**
     * Remove custom states assignments of a group.
     * @param group the group
     */
    public void removeServerGroup(ServerGroup group) {
        removeCustomStateAssignments(group);
    }

    /**
     * Remove custom states assignments of all servers in that org.
     * @param org the org
     */
    public void removeOrg(Org org) {
        List<Server> servers = MinionServerFactory.lookupByOrg(org.getId());
        for (Server server : servers) {
            removeServer(server);
        }
        removeCustomStateAssignments(org);
    }

    /**
     * Regenerate custom state assignments for org, group and severs where
     * the given state is used.
     * @param orgId org id
     * @param name custom state name
     */
    public void regenerateCustomStates(long orgId, String name) {
        StateFactory.CustomStateRevisionsUsage usage = StateFactory
                .latestStateRevisionsByCustomState(orgId, name);
        usage.getServerStateRevisions().forEach(rev ->
                generateServerCustomState(rev)
        );
        usage.getServerGroupStateRevisions().forEach(rev ->
                generateGroupCustomState(rev)
        );
        usage.getOrgStateRevisions().forEach(rev ->
                generateOrgCustomState(rev)
        );
    }

    /**
     * Regenerate pillar with the new org and create a new state revision without
     * any package or custom states.
     * @param server the migrated server
     * @param user the user performing the migration
     */
    public void migrateServer(Server server, User user) {
        // generate a new state revision without any package or custom states
        ServerStateRevision newStateRev = StateRevisionService.INSTANCE
                .cloneLatest(server, user, false, false);
        StateFactory.save(newStateRev);

        // refresh pillar, custom and package states
        generatePillarForServer(server);
        generateServerCustomState(newStateRev);
        StatesAPI.generateServerPackageState(server);
    }

}
