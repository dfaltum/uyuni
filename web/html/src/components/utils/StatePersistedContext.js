/* eslint-disable */
"use strict";

import PropTypes from 'prop-types';
import * as React from 'react';

const StatePersistedContext = React.createContext({loadState: undefined, saveState: undefined});

module.exports = {
    StatePersistedContext,
}
