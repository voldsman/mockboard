<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {useBoardStore} from "@/stores/boardStore.js";

const boardStore = useBoardStore();
const emit = defineEmits(['view-log'])

const isConnected = ref(false)
const statusMessage = ref('INITIALIZING')
const retryCount = ref(0)
const logs = ref([
    {id: 1, method: 'POST', path: '/api/v1/login', status: 200, matched: true, time: '14:20:01'},
    {id: 2, method: 'GET', path: '/users/123', status: 404, matched: false, time: '14:19:55'},
    {id: 3, method: 'GET', path: '/products', status: 200, matched: false, time: '14:18:22'},
    {id: 4, method: 'PUT', path: '/settings', status: 500, matched: true, time: '14:15:10'},
])

let eventSource = null
let reconnectTimer = null

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
        console.log(e)
        //const data = JSON.parse(e.data)
        // handle
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

onMounted(() => {
    if (boardStore.board?.id) {
        connectSSE();
    }
});

onUnmounted(() => {
    clearTimeout(reconnectTimer);
    if (eventSource) eventSource.close();
});

const handleLogClick = (log) => {
    emit('view-log', log)
}
</script>

<template>
    <aside class="sidebar-fixed">
        <div class="p-3 border-bottom bg-light d-flex justify-content-between align-items-center">
            <span class="text-uppercase text-muted text-xs fw-bold">Live Requests</span>

            <div class="d-flex align-items-center fw-bold text-xs"
                 :class="isConnected ? 'text-success' : 'text-warning'">
                <span v-if="isConnected" class="spinner-grow spinner-grow-sm me-1" role="status"></span>
                <i v-else-if="retryCount >= 3" class="bi bi-x-circle-fill me-1 text-danger"></i>
                <i v-else class="bi bi-exclamation-triangle-fill me-1"></i>
                {{ statusMessage }}
            </div>
        </div>

        <div class="list-group list-group-flush">
            <a v-for="log in logs" href="#" class="list-group-item list-group-item-action log-item matched p-3"
               @click="handleLogClick(log)">
                <div class="d-flex w-100 justify-content-between mb-1">
                    <span class="badge bg-success badge-method">{{ log.method }}</span>
                    <small class="text-muted font-mono">{{ log.time }}</small>
                </div>
                <div class="font-mono text-truncate mb-1 fw-bold">{{ log.path }}</div>
                <div class="d-flex align-items-center gap-2">
                    <span v-if="log.matched" class="badge bg-light text-success border border-success text-xs">
                        <i class="bi bi-check-circle-fill me-1"></i>Matched
                    </span>
                    <span v-else class="badge bg-light text-warning border border-warning text-xs">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>No Match
                    </span>
                    <span class="badge bg-light text-dark border text-xs ms-auto">{{ log.status }}</span>
                </div>
            </a>
        </div>
    </aside>
</template>