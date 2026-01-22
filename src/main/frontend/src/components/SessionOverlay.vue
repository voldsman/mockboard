<script setup>
import {onMounted, ref} from 'vue'
import constants from "@/constants.js";
import boardService from '@/services/boardService.js'
import {useBoardStore} from "@/stores/boardStore.js";
import {useToast} from '@/useToast.js'

const emit = defineEmits(['session-start', 'session-continue'])

const boardStore = useBoardStore()
const {success, error} = useToast()

const isLoading = ref(true)
const showModal = ref(false)
const isReturningUser = ref(false)
const capacityReached = ref(false)

const pendingBoardData = ref(null)
const activeBoards = ref(0)
const maxBoards = ref(constants.MAX_ACTIVE_BOARDS)

onMounted(async () => {
    try {
        try {
            const stats = await boardService.getStats()
            activeBoards.value = stats.activeBoards || 0

            if (activeBoards.value >= maxBoards.value) {
                capacityReached.value = true
            }
        } catch (statErr) {
            console.warn("Stats fetch failed", statErr)
        }

        const boardModel = await boardStore.restoreSession()
        if (boardModel) {
            if (!boardModel.shouldShowOverlay()) {
                boardStore.setBoard(boardModel)
                emit('session-continue')
                showModal.value = false
                return
            }

            pendingBoardData.value = boardModel
            isReturningUser.value = true
            showModal.value = true
        } else {
            isReturningUser.value = false

            if (activeBoards.value >= maxBoards.value) {
                capacityReached.value = true
            }
            showModal.value = true
        }
    } catch (e) {
        console.error("Init failed", e)
        showModal.value = true
    } finally {
        isLoading.value = false
    }
})

const handleStartNew = async () => {
    try {
        await boardStore.createNewBoard()
        success('Board created')
        showModal.value = false
        emit('session-start')
    } catch (err) {
        if (err.status === 403) {
            capacityReached.value = true
            activeBoards.value = maxBoards.value
            error('Capacity reached while you were waiting.')
        } else {
            error('Failed to create board.')
        }
    }
}

const handleContinue = () => {
    if (pendingBoardData.value) {
        boardStore.setBoard(pendingBoardData.value)
        emit('session-continue')
    }
}

const reloadPage = () => {
    window.location.reload();
}
</script>

<template>
    <div v-if="showModal" class="fixed-top h-100 w-100 d-flex align-items-center justify-content-center bg-light">

        <div class="card border-0 shadow-sm" style="width: 100%; max-width: 480px;">
            <div class="card-header bg-white border-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <img src="/logo.png" alt="logo" width="35" height="35" class="opacity-75"/>
                    <span class="fw-bold small">MockBoard.dev</span>
                </div>
                <div class="badge bg-light text-dark border fw-normal py-2 px-3 rounded-pill">
                    <span class="status-dot me-2" :class="capacityReached ? 'bg-danger' : 'bg-success'"></span>
                    <span>System Capacity: {{ activeBoards }} / {{ maxBoards }}</span>
                </div>
            </div>

            <div class="card-body p-4 p-md-5">
                <div v-if="capacityReached && !isReturningUser" class="text-center py-4">
                    <div class="mb-3 text-muted">
                        <i class="bi bi-cloud-slash fs-1"></i>
                    </div>
                    <h3 class="fw-bold mb-3">Capacity Reached</h3>
                    <p class="text-muted mb-4">
                        Our server is currently handling the maximum number of active boards ({{maxBoards}}).
                        Please try again in a few minutes.
                    </p>
                    <button
                        @click="reloadPage"
                        class="btn btn-outline-dark w-100 py-2">
                        Check Availability
                    </button>
                </div>

                <div v-else>
                    <div class="mb-4">
                        <h2 class="fw-bold mb-2">{{ isReturningUser ? 'Welcome Back' : 'Get Started' }}</h2>
                        <p class="text-muted small mb-0" v-if="isReturningUser">
                            We found a saved session on this device.
                        </p>
                        <p class="text-muted small mb-0" v-else>
                            Ephemeral API mocking. No signups, no persistence.
                        </p>
                    </div>

                    <div class="d-grid gap-3 mb-4">
                        <button
                            v-if="isReturningUser"
                            class="btn btn-dark py-3 fw-semibold shadow-sm"
                            @click="handleContinue">
                            Restore Session
                        </button>

                        <button
                            class="btn py-3 fw-semibold"
                            :class="isReturningUser ? 'btn-outline-secondary' : 'btn-dark shadow-sm'"
                            @click="handleStartNew">
                            {{ isReturningUser ? 'Discard & Create New' : 'Create New Board' }}
                        </button>
                    </div>

<!--                    For web version, commenting out for self host-->
<!--                    <div class="bg-light p-3 rounded-2 text-center border border-secondary border-opacity-10">-->
<!--                        <p class="mb-1 small fw-bold text-dark">-->
<!--                            <i class="bi bi-clock me-1"></i> Hard Reset at 03:00 UTC-->
<!--                        </p>-->
<!--                        <p class="text-muted small mb-3" style="font-size: 0.8rem;">-->
<!--                            All data is vaporized daily. Don't get attached.-->
<!--                        </p>-->
<!--                        <router-link to="/fair-use" class="small text-decoration-underline text-secondary">-->
<!--                            Fair Use Policy-->
<!--                        </router-link>-->
<!--                    </div>-->
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.status-dot {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-bottom: 1px;
}
</style>