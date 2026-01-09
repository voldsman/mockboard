<script setup>
import {reactive, ref} from "vue";
import {useToast} from "@/useToast.js";
import {useBoardStore} from "@/stores/boardStore.js";
import uiHelper from "@/helpers/uiHelper.js";

const {success, error} = useToast();
const boardStore = useBoardStore();
const emit = defineEmits(['close']);

const formData = reactive({
    method: 'GET',
    path: '',
    headers: [{ key: 'Content-Type', value: 'application/json' }],
    body: '',
    statusCode: 200,
    delay: 0
});

const errors = ref({});

const addHeader = () => {
    if (!Array.isArray(formData.headers)) formData.headers = [];
    formData.headers.push({ key: '', value: '' });
};

const removeHeader = (index) => {
    formData.headers.splice(index, 1);
};

const isValidJson = (str) => {
    if (!str || str.trim() === '') return true;
    try {
        JSON.parse(str);
        return true;
    } catch (e) {
        return false;
    }
};

const validate = () => {
    const errs = {};
    if (!formData.path) errs.path = "Path is required";
    if (formData.path && !formData.path.startsWith('/')) errs.path = "Path must start with /";

    const patternCount = (formData.path.match(/\*/g) || []).length;
    if (patternCount > 3) errs.path = "Max 3 wildcards allowed";

    if (Array.isArray(formData.headers)) {
        formData.headers.forEach((h, i) => {
            if (h.key && !h.value) errs[`header_${i}`] = "Value required";
        });
    }

    if (!isValidJson(formData.body)) {
        errs.body = "Body must be valid JSON format";
    }

    errors.value = errs;
    return Object.keys(errs).length === 0;
};

const handleSubmit = async () => {
    console.log('Submit clicked');

    if (!validate()) {
        console.log('Validation failed:', errors.value);
        return;
    }

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

        console.log('Sending payload:', payload);
        await boardStore.createNewMockRule(payload);
        success("Mock rule created successfully!");
        emit('close');
    } catch (err) {
        console.error('Error creating mock:', err);
        error("Failed to save: " + (err.response?.data?.message || err.message || "Unknown error"));
    }
};

const handleCancel = () => {
    emit('close')
}
</script>

<template>
    <div id="view-create-mock">
        <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
            <div class="d-flex align-items-center gap-3">
                <h4 class="fw-bold mb-0 text-dark">Create Mock Endpoint</h4>
            </div>
            <div class="d-flex gap-2">
                <button
                    @click="handleCancel"
                    type="button"
                    class="btn btn-outline-secondary btn-sm">Cancel</button>
                <button
                    @click="handleSubmit"
                    type="button"
                    class="btn btn-primary px-4 fw-bold"> Create Mock
                </button>
            </div>
        </div>

        <form @submit.prevent="handleSubmit" id="mockForm">
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
                        </select>
                    </div>
                    <div class="flex-grow-1">
                        <input v-model="formData.path" type="text" class="form-control border-0 font-mono py-3" placeholder="/api/v1/resource/*">
                    </div>
                </div>
                <div v-if="errors.path" class="text-danger small mt-1">{{ errors.path }}</div>
                <div class="form-text text-muted small mt-2">
                    Use <span class="badge bg-light text-dark border font-mono">*</span> as a wildcard. Max 3 wildcards per path.
                </div>
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold text-muted small text-uppercase">Response Headers</label>
                <div class="bg-white border rounded shadow-sm">
                    <table class="table table-sm table-borderless mb-0">
                        <tbody class="font-mono border-bottom">
                        <tr v-for="(header, index) in formData.headers" :key="index">
                            <td class="ps-3 py-2">
                                <input v-model="header.key" type="text" class="form-control form-control-sm border-0" placeholder="Header Key">
                            </td>
                            <td class="py-2">
                                <input v-model="header.value" type="text" class="form-control form-control-sm border-0" placeholder="Value">
                            </td>
                            <td class="text-center pt-2" style="width: 50px;">
                                <button type="button" @click="removeHeader(index)" class="btn btn-link btn-sm text-danger p-0">
                                    <i class="bi bi-x-lg"></i>
                                </button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="p-2 border-top bg-light-subtle">
                        <button type="button" @click="addHeader" class="btn btn-link btn-sm text-primary text-decoration-none fw-bold small">
                            + Add Header
                        </button>
                    </div>
                </div>
            </div>

            <div class="mb-4">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <label class="form-label fw-bold text-muted small text-uppercase mb-0">Response Body (JSON)</label>
                </div>
                <textarea v-model="formData.body"
                          class="form-control font-mono p-3 border text-success"
                          :class="{'border-danger': errors.body}"
                          rows="8"
                          placeholder='{ "message": "Hello World" }'></textarea>
                <div v-if="errors.body" class="text-danger small mt-1">{{ errors.body }}</div>
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
                        <option :value="1000">1s (Heavy load)</option>
                        <option :value="3000">3s (Timeout sim)</option>
                    </select>
                </div>
            </div>
        </form>
    </div>
</template>