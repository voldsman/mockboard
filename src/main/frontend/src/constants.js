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
    DASHBOARD_VIEWS: {
        DASHBOARD: 'dashboard',
        CREATE_MOCK: 'create_mock',
        EDIT_MOCK: 'edit_mock',
        LOG_DETAILS: 'log_details',
    },

    // validation
    MAX_MOCKS: 12,
    VALIDATION: {
        MAX_PATH_LENGTH: 200,
        MAX_BODY_LENGTH: 5000,
        MAX_WILDCARDS: 3,
        MAX_HEADERS: 5,
        MAX_HEADER_KEY_LENGTH: 100,
        MAX_HEADER_VALUE_LENGTH: 500,
        VALID_HTTP_METHODS: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'],
        VALID_PATH_PATTERN: /^\/[a-zA-Z0-9/_\-*{}]+$/,
    }
}
