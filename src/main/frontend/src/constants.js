export default {
    // base
    APP_VERSION: '0.0.1-dev',
    SERVER_URL: 'http://localhost:8000',

    // ls
    BOARD_DATA: 'BOARD_DATA',
    SESSION_OVERLAY_REASK_TTL_LS_KEY: 'OVERLAY_REASK_TTL',

    // ttl
    REASK_OVERLAY_TTL: 60 * 60 * 1000,

    // http
    OWNER_TOKEN_HEADER_KEY: 'X-Owner-Token',

    // app
    MAX_MOCKS: 12,
    DASHBOARD_VIEWS: {
        DASHBOARD: 'dashboard',
        CREATE_MOCK: 'create_mock',
        EDIT_MOCK: 'edit_mock',
        LOG_DETAILS: 'log_details',
    },
}
