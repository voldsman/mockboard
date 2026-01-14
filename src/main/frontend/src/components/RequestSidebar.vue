<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {useBoardStore} from "@/stores/boardStore.js";
import uiHelper from "@/helpers/uiHelper.js";
import {storeToRefs} from "pinia";
import {selectionState} from "@/helpers/selectionState.js";

const boardStore = useBoardStore();
const emit = defineEmits(['view-webhook'])

const isConnected = ref(false)
const statusMessage = ref('INITIALIZING')
const retryCount = ref(0)
const currentTime = ref(Date.now())

const {webhooks} = storeToRefs(boardStore)

let eventSource = null
let reconnectTimer = null
let webhookTimestampTimer = null

const BACKOFF_STRATEGY = [10_000, 20_000, 30_000];

const connectSSE = () => {
    if (eventSource) eventSource.close();

    const url = `/api/boards/${boardStore.board.id}/stream?token=${boardStore.board.ownerToken}`;
    eventSource = new EventSource(url);

    eventSource.onopen = () => {
        isConnected.value = true;
        retryCount.value = 0;
        statusMessage.value = 'LISTENING';
        console.log('SSE connected');
    };

    eventSource.addEventListener('webhook-event', (e) => {
        try {
            const data = JSON.parse(e.data)
            boardStore.processReceivedWebhook(data)
        } catch (err) {
            console.error('Could not parse the event', err);
        }
    })

    eventSource.addEventListener('server-shutdown', () => {
        statusMessage.value = 'SERVER RESTARTING';
        isConnected.value = false;

        eventSource.close();
        handleReconnect();
    });

    eventSource.onerror = (err) => {
        isConnected.value = false;
        handleReconnect();
    };
}

const handleReconnect = () => {
    if (eventSource) eventSource.close();

    if (retryCount.value < BACKOFF_STRATEGY.length) {
        const delay = BACKOFF_STRATEGY[retryCount.value];
        statusMessage.value = `RECONNECTING IN ${delay / 1000}s...`;

        retryCount.value++;

        clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(() => {
            connectSSE();
        }, delay);
    } else {
        statusMessage.value = 'CONNECTION LOST';
        console.error('Max reconnect attempts reached.');
    }
};

onMounted(async () => {
    if (boardStore.board?.id) {
        await boardStore.fetchWebhooks()
        connectSSE();
    }
    webhookTimestampTimer = setInterval(() => {
        currentTime.value = Date.now();
    }, 20000)
});

onUnmounted(() => {
    clearTimeout(webhookTimestampTimer)
    clearTimeout(reconnectTimer);
    if (eventSource) eventSource.close();
});

const handleWebhookClick = (log) => {
    selectionState.select(log.id)
    emit('view-webhook', log)
}
</script>

<template>
    <aside class="sidebar-fixed">
        <div class="p-3 border-bottom bg-light d-flex justify-content-between align-items-center">
            <span class="text-uppercase text-muted text-xs fw-bold">Live Requests</span>

            <div :class="isConnected ? 'text-success' : 'text-warning'"
                 class="d-flex align-items-center fw-bold text-xs">
                <span v-if="isConnected" class="spinner-grow spinner-grow-sm me-1" role="status"></span>
                <i v-else-if="retryCount >= 3" class="bi bi-x-circle-fill me-1 text-danger"></i>
                <i v-else class="bi bi-exclamation-triangle-fill me-1"></i>
                {{ statusMessage }}
            </div>
        </div>

        <div class="list-group list-group-flush">
            <div v-if="webhooks.length === 0" class="text-center py-5 bg-white rounded-3 border border-dashed">
                <h6 class="text-dark">No Activity Yet</h6>
                <small class="text-muted">
                    Send a request to your mock URL <br> to see the payload in real-time.
                </small>
            </div>

            <a v-for="webhook in webhooks"
               :key="webhook.id"
               class="list-group-item list-group-item-action log-item p-3 border-0 border-bottom cursor-pointer"
               :class="{ 'request-selected text-white': selectionState.activeId === webhook.id }"
               @click="handleWebhookClick(webhook)">

                <div class="d-flex w-100 justify-content-between mb-1 align-items-center">
                    <span :class="['badge badge-method', uiHelper.getMethodColor(webhook.method)]">
                        {{ webhook.method }}
                    </span>
                    <small class="text-muted font-mono">
                        {{ uiHelper.formatWebhookTime(webhook.timestamp) }}
                    </small>
                </div>
                <div class="font-mono text-truncate mb-2 fw-bold text-dark">
                    {{ webhook.path }}
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span v-if="webhook.matched" class="badge bg-light text-success border border-success-subtle text-xs">
                        <i class="bi bi-check-circle-fill me-1"></i>Matched
                    </span>
                    <span v-else class="badge bg-warning text-light border border-warning text-xs">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>No Match
                    </span>

                    <span class="ms-auto d-flex align-items-center gap-2">
                        <small class="text-muted text-xs font-mono">{{ webhook.processingTimeMs || 0 }}ms</small>
                        <span class="badge bg-light text-dark border text-xs">{{ webhook.statusCode }}</span>
                    </span>
                </div>
            </a>
        </div>
    </aside>
</template>

<style scoped>
.request-selected {
    background-color: #e6ddea !important;
    border-left: 4px solid #9d71b6 !important;
    box-shadow: inset 0 0 10px rgba(0,0,0,0.1);
}
</style>