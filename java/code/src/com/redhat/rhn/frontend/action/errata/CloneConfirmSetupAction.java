/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.errata;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.manager.errata.ErrataManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CloneConfirmSetupAction
 */
public class CloneConfirmSetupAction extends RhnListAction {
    /** This class reuses code in the channel assignment page */
    private ChannelSetupAction channelSetupAction = new ChannelSetupAction();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm formIn, HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);

        User user = requestContext.getCurrentUser();
        PageControl pc = new PageControl();

        clampListBounds(pc, request, user);

        DataResult dr = getDataResult(user, pc);

        request.setAttribute("errataList", dr);

        return channelSetupAction.execute(mapping, formIn, request, response);
    }

    /**
     * {@inheritDoc}
     */
    protected DataResult getDataResult(User user, PageControl pc) {
        return ErrataManager.selectedForCloning(user, pc);
    }

}
