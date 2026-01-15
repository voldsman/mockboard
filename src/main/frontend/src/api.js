import axios from "axios";
import constants from "./constants";

const api = axios.create({
    baseURL: '/'
})

let ownerToken = null
export function setOwnerToken(token) {
    ownerToken = token;
}

api.interceptors.request.use((config) => {
    config.headers = config.headers || {};
    config.headers['Content-Type'] = 'application/json';
    if (ownerToken) {
        config.headers[constants.OWNER_TOKEN_HEADER_KEY] = ownerToken;
    }
    return config;
})

export default api;