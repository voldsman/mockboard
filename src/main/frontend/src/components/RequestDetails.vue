<script setup>

import uiHelper from "@/helpers/uiHelper.js";
import {computed} from "vue";
import {selectionState} from "@/helpers/selectionState.js";

const emit = defineEmits(['close']);
const props = defineProps({
    webhook: {
        type: Object,
        required: true,
        validator: (value) => value.id != null
    }
});

const parsedQueryParams = computed(() => {
    if (!props.webhook.queryParams) return []

    return props.webhook.queryParams
        .split("&")
        .filter(Boolean)
        .map(param => {
            const [key, value] = param.split("=");
            return {
                key: decodeURIComponent(key || ''),
                value: decodeURIComponent(value || ''),
            }
        })
})

const parsedHeaders = computed(() => {
    const raw = props.webhook.headers;
    if (!raw) return [];
    return Object.entries(typeof raw === 'string' ? JSON.parse(raw) : raw)
        .map(([key, value]) => ({ key, value }))
})

const formattedBody = computed(() => {
    try {
        const body = props.webhook.body;
        return typeof body === 'string'
            ? JSON.stringify(JSON.parse(body), null, 2)
            : JSON.stringify(body, null, 2);
    } catch (e) {
        return props.webhook.body || '{}'
    }
})

const handleClose = () => {
    selectionState.clear();
    emit('close')
}
</script>

<template>
    <div>
        <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
            <div class="d-flex align-items-center gap-3">
                <h4 class="fw-bold mb-0 text-dark">Request Details</h4>
                <span class="badge bg-light text-muted font-mono border">ID: {{ webhook.id }}</span>
            </div>
            <div class="d-flex gap-2">
                <button @click="handleClose" type="button" class="btn btn-outline-secondary btn-sm">
                    Close
                </button>
            </div>
        </div>

        <div class="mb-4">
            <label class="form-label fw-bold text-muted small text-uppercase">Request Received</label>
            <div class="bg-white border rounded shadow-sm overflow-hidden d-flex">
                <div class="border-end bg-light-subtle d-flex align-items-center justify-content-center" style="width: 120px;">
                    <span class="font-mono fw-bold py-3 px-3"
                          :class="uiHelper.getMethodColor(webhook.method).replace('bg-', 'text-')">
                        {{ webhook.method }}
                    </span>
                </div>
                <div class="flex-grow-1 bg-white">
                    <div class="form-control border-0 font-mono py-3 text-dark bg-transparent text-break">
                        {{ webhook.path }}
                    </div>
                </div>
                <div class="border-start bg-light-subtle d-flex align-items-center px-3">
                     <span :class="['fw-bold font-mono', webhook.statusCode < 400 ? 'text-success' : 'text-danger']">
                        {{ webhook.statusCode }}
                    </span>
                </div>
            </div>
        </div>

        <div class="mb-4" v-if="parsedQueryParams.length > 0">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label fw-bold text-muted small text-uppercase">Request Query Params</label>
                <small class="text-muted font-mono">{{ parsedQueryParams.length }} items</small>
            </div>
            <div class="bg-white border rounded shadow-sm">
                <table class="table table-sm table-borderless mb-0">
                    <tbody class="font-mono">
                    <tr v-for="(param, index) in parsedQueryParams" :key="index" class="border-bottom last-child-border-0">
                        <td class="ps-3 py-2 text-muted fw-bold text-break" style="width: 30%;">
                            {{ param.key }}
                        </td>
                        <td class="py-2 text-dark text-break">
                            {{ param.value }}
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="mb-4">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label fw-bold text-muted small text-uppercase">Request Headers</label>
                <small class="text-muted font-mono">{{ parsedHeaders.length }} items</small>
            </div>
            <div class="bg-white border rounded shadow-sm">
                <table class="table table-sm table-borderless mb-0">
                    <tbody class="font-mono">
                    <tr v-for="(header, index) in parsedHeaders" :key="index" class="border-bottom last-child-border-0">
                        <td class="ps-3 py-2 text-muted fw-bold text-break" style="width: 30%;">
                            {{ header.key }}
                        </td>
                        <td class="py-2 text-dark text-break">
                            {{ header.value }}
                        </td>
                    </tr>
                    <tr v-if="parsedHeaders.length === 0">
                        <td colspan="2" class="text-center text-muted py-3 small">No headers present</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="mb-4">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label fw-bold text-muted small text-uppercase mb-0">Request payload</label>
                <small class="text-muted font-mono">{{ webhook.contentType }}</small>
            </div>
            <pre class="form-control font-mono p-3 border bg-dark text-success mb-0"
                 style="height: 300px; overflow-y: auto; cursor: default;">{{ formattedBody }}</pre>
        </div>

        <div class="row g-3">
            <div class="col-md-4">
                <div class="p-2 border rounded bg-light-subtle font-mono small d-flex align-items-center gap-2">
                    <span class="text-muted">MATCH:</span>
                    <span v-if="webhook.matched" class="text-success fw-bold">
                        <i class="bi bi-check-circle-fill me-1"></i>MATCHED
                    </span>
                    <span v-else class="text-warning fw-bold">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>NO MATCH
                    </span>
                </div>
            </div>
            <div class="col-md-4">
                <div class="p-2 border rounded bg-light-subtle font-mono small text-center">
                    <span class="text-muted">PROCESS:</span> {{ webhook.processingTimeMs || 0 }}ms
                </div>
            </div>
            <div class="col-md-4">
                <div class="p-2 border rounded bg-light-subtle font-mono small text-end">
                    <span class="text-muted">RECEIVED:</span> {{ new Date(webhook.timestamp).toLocaleTimeString() }}
                </div>
            </div>
        </div>
    </div>
</template>