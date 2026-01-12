<script setup>
import {computed, onMounted, reactive, ref, watch} from "vue";
import {useToast} from "@/useToast.js";
import {useBoardStore} from "@/stores/boardStore.js";
import uiHelper from "@/helpers/uiHelper.js";
import {getCharacterCount, getLimitStatus, validateMockRule} from "@/helpers/mockRuleValidator.js";
import constants from "@/constants.js";

const props = defineProps({
    mode: {
        type: String,
        default: 'create',
        validator: (value) => ['create', 'edit'].includes(value)
    },
    mockRuleId: {
        type: String,
        default: null
    }
});

const {success, error} = useToast();
const boardStore = useBoardStore();
const emit = defineEmits(['close']);

const formData = reactive({
    method: 'GET',
    path: '',
    headers: [{ key: 'Content-Type', value: 'application/json' }],
    body: '{}',
    statusCode: 200,
    delay: 0
});

const errors = ref({});
const isLoading = ref(false);

const pathLimit = computed(() => getLimitStatus(formData.path?.length || 0, constants.VALIDATION.MAX_PATH_LENGTH));
const bodyLimit = computed(() => {
    const { bytes } = getCharacterCount(formData.body);
    return getLimitStatus(bytes, constants.VALIDATION.MAX_BODY_LENGTH);
});
const headersCount = computed(() => {
    const nonEmpty = formData.headers.filter(h => h.key && h.key.trim() !== '').length;
    return getLimitStatus(nonEmpty, constants.VALIDATION.MAX_HEADERS);
});

onMounted(() => {
    if (props.mode === 'edit' && props.mockRuleId) {
        loadMockRule();
    }
});

watch(() => props.mockRuleId, (newId) => {
    if (props.mode === 'edit' && newId) {
        loadMockRule();
    }
});

const loadMockRule = () => {
    const mockRule = boardStore.mockRules.find(r => r.id === props.mockRuleId);
    if (!mockRule) {
        error('Mock rule not found');
        emit('close');
        return;
    }

    formData.method = mockRule.method;
    formData.path = mockRule.path;
    formData.statusCode = mockRule.statusCode;
    formData.delay = mockRule.delay || 0;

    try {
        const headersObj = mockRule.headers ? JSON.parse(mockRule.headers) : {};
        formData.headers = Object.entries(headersObj).map(([key, value]) => ({ key, value }));
        if (formData.headers.length === 0) {
            formData.headers = [{ key: 'Content-Type', value: 'application/json' }];
        }
    } catch (e) {
        formData.headers = [{ key: 'Content-Type', value: 'application/json' }];
    }
    formData.body = mockRule.body || '';
};

const addHeader = () => {
    if (!Array.isArray(formData.headers)) formData.headers = [];
    formData.headers.push({ key: '', value: '' });
};

const removeHeader = (index) => {
    formData.headers.splice(index, 1);
    delete errors.value[`header_${index}`];
};

const validate = () => {
    errors.value = validateMockRule(formData);
    return Object.keys(errors.value).length === 0;
};

const handleSubmit = async () => {
    if (!validate()) {
        console.log('Validation failed:', errors.value);
        return;
    }

    isLoading.value = true;

    try {
        const headersObj = {};
        if (Array.isArray(formData.headers)) {
            formData.headers.forEach(h => {
                if (h.key && h.value) headersObj[h.key] = h.value;
            });
        }

        const payload = {
            method: formData.method,
            path: formData.path,
            statusCode: formData.statusCode,
            delay: formData.delay,
            headers: JSON.stringify(headersObj),
            body: formData.body
        };

        if (props.mode === 'edit') {
            await boardStore.updateMockRuleById(props.mockRuleId, payload);
            success("Mock rule updated successfully!");
        } else {
            await boardStore.createNewMockRule(payload);
            success("Mock rule created successfully!");
        }

        emit('close');
    } catch (err) {
        console.error('Error saving mock:', err);
        error("Failed to save: " + (err.response?.data?.message || err.message || "Unknown error"));
    } finally {
        isLoading.value = false;
    }
};

const handleCancel = () => {
    emit('close');
}
</script>

<template>
    <div>
        <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
            <div class="d-flex align-items-center gap-3">
                <h4 class="fw-bold mb-0 text-dark">
                    {{ mode === 'edit' ? 'Edit Mock Endpoint' : 'Create Mock Endpoint' }}
                </h4>
            </div>
            <div class="d-flex gap-2">
                <button
                    @click="handleCancel"
                    type="button"
                    class="btn btn-outline-secondary btn-sm"
                    :disabled="isLoading">
                    Cancel
                </button>
                <button
                    @click="handleSubmit"
                    type="button"
                    class="btn btn-primary px-4 fw-bold"
                    :disabled="isLoading">
                    <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
                    {{ mode === 'edit' ? 'Update Mock' : 'Create Mock' }}
                </button>
            </div>
        </div>

        <form @submit.prevent="handleSubmit">
            <div class="mb-4">
                <label class="form-label fw-bold text-muted small text-uppercase">Request Match</label>
                <div :class="['bg-white border rounded shadow-sm overflow-hidden d-flex', errors.path ? 'border-danger' : '']">
                    <div class="border-end bg-light-subtle" style="width: 120px;">
                        <select v-model="formData.method"
                                class="form-select border-0 font-mono fw-bold py-3 bg-transparent text-center h-100"
                                :class="uiHelper.getMethodColor(formData.method).replace('bg-', 'text-')">
                            <option>GET</option>
                            <option>POST</option>
                            <option>PUT</option>
                            <option>DELETE</option>
                            <option>PATCH</option>
                            <option>OPTIONS</option>
                            <option>HEAD</option>
                        </select>
                    </div>
                    <div class="flex-grow-1">
                        <input v-model="formData.path"
                               type="text"
                               class="form-control border-0 font-mono py-3"
                               placeholder="/api/v1/resource/*"
                               :maxlength="constants.VALIDATION.MAX_PATH_LENGTH">
                    </div>
                </div>
                <div v-if="errors.path" class="text-danger small mt-1">{{ errors.path }}</div>
                <div class="d-flex justify-content-between align-items-center mt-2">
                    <div class="form-text text-muted small">
                        Use <span class="badge bg-light text-dark border font-mono">*</span> as wildcard.
                        Max {{ constants.VALIDATION.MAX_WILDCARDS }} wildcards per path.
                    </div>
                    <small :class="['font-mono', pathLimit.isNearLimit ? 'text-warning' : 'text-muted']">
                        {{ pathLimit.current }}/{{ pathLimit.max }}
                    </small>
                </div>
            </div>

            <div class="mb-4">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <label class="form-label fw-bold text-muted small text-uppercase">Response Headers</label>
                    <small :class="['font-mono', headersCount.isNearLimit ? 'text-warning fw-bold' : 'text-muted']">
                        {{ headersCount.current }}/{{ headersCount.max }}
                    </small>
                </div>
                <div class="bg-white border rounded shadow-sm" :class="{'border-warning': headersCount.isAtLimit}">
                    <table class="table table-sm table-borderless mb-0">
                        <tbody class="font-mono border-bottom">
                        <tr v-for="(header, index) in formData.headers" :key="index">
                            <td class="ps-3 py-2">
                                <input v-model="header.key"
                                       type="text"
                                       class="form-control form-control-sm border-0"
                                       :class="{'border border-danger': errors[`header_${index}`]}"
                                       placeholder="Header Key"
                                       :maxlength="constants.VALIDATION.MAX_HEADER_KEY_LENGTH">
                            </td>
                            <td class="py-2">
                                <input v-model="header.value"
                                       type="text"
                                       class="form-control form-control-sm border-0"
                                       :class="{'border border-danger': errors[`header_${index}`]}"
                                       placeholder="Value"
                                       :maxlength="constants.VALIDATION.MAX_HEADER_VALUE_LENGTH">
                            </td>
                            <td class="text-center pt-2" style="width: 50px;">
                                <button type="button" @click="removeHeader(index)" class="btn btn-link btn-sm text-danger p-0">
                                    <i class="bi bi-x-lg"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="formData.headers.length === 0">
                            <td colspan="3" class="text-center text-muted py-3 small">No headers defined</td>
                        </tr>
                        </tbody>
                    </table>
                    <div v-if="errors.headers" class="px-3 pb-2">
                        <small class="text-danger">{{ errors.headers }}</small>
                    </div>
                    <div class="p-2 border-top bg-light-subtle">
                        <button
                            type="button"
                            @click="addHeader"
                            :disabled="headersCount.isAtLimit"
                            class="btn btn-link btn-sm text-primary text-decoration-none fw-bold small"
                            :class="{'disabled text-muted': headersCount.isAtLimit}">
                            <i class="bi bi-plus-circle me-1"></i>
                            Add Header
                            <span v-if="headersCount.isAtLimit" class="text-danger">(Max reached)</span>
                        </button>
                    </div>
                </div>
            </div>

            <div class="mb-4">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <label class="form-label fw-bold text-muted small text-uppercase mb-0">Response Body (JSON)</label>
                    <small :class="['font-mono', bodyLimit.isNearLimit ? 'text-warning fw-bold' : 'text-muted']">
                        {{ (bodyLimit.current / 1000).toFixed(1) }}KB / {{ (bodyLimit.max / 1000).toFixed(1) }}KB
                    </small>
                </div>
                <textarea v-model="formData.body"
                          class="form-control font-mono p-3 border bg-dark text-success"
                          :class="{'border-danger': errors.body, 'border-warning': bodyLimit.isNearLimit && !errors.body}"
                          rows="8"
                          placeholder='{ "message": "Hello World" }'></textarea>
                <div v-if="errors.body" class="text-danger small mt-1">{{ errors.body }}</div>
                <div v-else-if="bodyLimit.isNearLimit" class="text-warning small mt-1">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>Approaching size limit
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-md-6">
                    <label class="form-label fw-bold text-muted small text-uppercase">Status Code</label>
                    <select v-model.number="formData.statusCode" class="form-select border bg-white p-2 font-mono fw-bold">
                        <optgroup label="Success">
                            <option :value="200">200 OK</option>
                            <option :value="201">201 Created</option>
                            <option :value="204">204 No Content</option>
                        </optgroup>
                        <optgroup label="Client Error">
                            <option :value="400">400 Bad Request</option>
                            <option :value="401">401 Unauthorized</option>
                            <option :value="403">403 Forbidden</option>
                            <option :value="404">404 Not Found</option>
                            <option :value="429">429 Too Many Requests</option>
                        </optgroup>
                        <optgroup label="Server Error">
                            <option :value="500">500 Internal Server Error</option>
                            <option :value="503">503 Service Unavailable</option>
                        </optgroup>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label fw-bold text-muted small text-uppercase">Delay</label>
                    <select v-model.number="formData.delay" class="form-select border bg-white p-2 font-mono fw-bold">
                        <option :value="0">No delay</option>
                        <option :value="250">250ms</option>
                        <option :value="500">500ms</option>
                        <option :value="1000">1s</option>
                        <option :value="3000">3s</option>
                        <option :value="5000">5s</option>
                        <option :value="10000">10s</option>
                    </select>
                </div>
            </div>
        </form>
    </div>
</template>