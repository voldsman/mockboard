<script setup>
import {onMounted, ref} from 'vue'
import constants from "@/constants.js";
import boardService from '@/services/boardService.js'
import {BoardModel} from "@/models/boardModel.js";
import {useBoardStore} from "@/stores/boardStore.js";
import {useToast} from '@/useToast.js'

const emit = defineEmits(['session-start', 'session-continue'])

const boardStore = useBoardStore()
const {success, error} = useToast()

const showModal = ref(false)
const isReturningUser = ref(false)
const pendingBoardData = ref(null)

onMounted(async () => {
    const boardModel = BoardModel.fromLS(constants.BOARD_DATA);

    if (boardModel && boardModel.id && !boardModel.isExpired()) {
        try {
            const result = await boardService.checkExistence(boardModel.id, boardModel.ownerToken);
            const validatedBoard = new BoardModel(result.data);

            console.log(boardModel);
            if (!boardModel.shouldShowOverlay()) {
                boardStore.initializeBoardStore(validatedBoard)
                emit('session-continue')
                return;
            }

            pendingBoardData.value = validatedBoard;
            isReturningUser.value = true;
        } catch (err) {
            console.warn('Session expired on server or invalid data');
            isReturningUser.value = false
            boardStore.clearBoardStore()
        }
    } else {
        console.log('No local data found or session expired');
        isReturningUser.value = false
        boardStore.clearBoardStore()
    }

    showModal.value = true
})

const handleStartNew = async () => {
    try {
        const result = await boardService.createBoard()
        const boardModel = new BoardModel(result.data)
        boardStore.initializeBoardStore(boardModel)

        showModal.value = false
        success('Board created successfully!')
        emit('session-start')
    } catch (err) {
        console.error('Failed to create new board', err);
        error('Failed to create board. Please try again.'+ err)
    }
}

const handleContinue = () => {
    if (pendingBoardData.value) {
        const model = pendingBoardData.value
        boardStore.initializeBoardStore(model)
    }
    showModal.value = false
    success('Welcome back!')
    emit('session-continue')
}
</script>

<template>
    <div v-if="showModal" class="session-overlay">
        <div class="card border-0 shadow-lg overflow-hidden w-100"
             style="max-width: 900px; min-height: 450px; border-radius: 0.75rem;">
            <div class="row g-0 h-100">

                <div class="col-md-5 bg-light border-end d-flex flex-column justify-content-between p-4 p-md-5">
                    <div>
                        <div class="d-flex align-items-center gap-2 mb-4 fw-bold fs-4">
                            <i class="bi bi-box-seam-fill text-primary"></i>
                            <span>
                                MockBoard<span class="text-muted fw-light">.dev</span>
                            </span>
                        </div>

                        <div class="d-flex flex-column gap-4">
                            <div class="d-flex gap-3">
                                <div class="text-warning mt-1">
                                    <svg class="bi" fill="none" height="20" stroke="currentColor" viewBox="0 0 24 24"
                                         width="20">
                                        <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round"
                                              stroke-linejoin="round"
                                              stroke-width="2"></path>
                                    </svg>
                                </div>
                                <div>
                                    <h6 class="fw-bold text-dark mb-1" style="font-size: 0.875rem;">Disposable
                                        Workspace</h6>
                                    <p class="text-muted mb-0" style="font-size: 0.75rem; line-height: 1.5;">
                                        Everything is ephemeral. All data is automatically wiped daily at <span
                                        class="font-mono text-dark">03:00 UTC</span>.
                                    </p>
                                </div>
                            </div>

                            <div class="d-flex gap-3">
                                <div class="text-primary mt-1">
                                    <svg class="bi" fill="none" height="20" stroke="currentColor" viewBox="0 0 24 24"
                                         width="20">
                                        <path d="M13 10V3L4 14h7v7l9-11h-7z" stroke-linecap="round"
                                              stroke-linejoin="round"
                                              stroke-width="2"></path>
                                    </svg>
                                </div>
                                <div>
                                    <h6 class="fw-bold text-dark mb-1" style="font-size: 0.875rem;">Strict Rate
                                        Limits</h6>
                                    <p class="text-muted mb-0" style="font-size: 0.75rem; line-height: 1.5;">
                                        Fair use policy applies. Mock execution and creation are rate-limited per API
                                        Key.
                                    </p>
                                </div>
                            </div>

                            <div class="d-flex gap-3">
                                <div class="text-secondary mt-1">
                                    <svg class="bi" fill="none" height="20" stroke="currentColor" viewBox="0 0 24 24"
                                         width="20">
                                        <path
                                            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                                            stroke-linecap="round" stroke-linejoin="round"
                                            stroke-width="2"></path>
                                    </svg>
                                </div>
                                <div>
                                    <div class="d-flex align-items-center gap-2">
                                        <h6 class="fw-bold text-dark mb-1" style="font-size: 0.875rem;">Private
                                            Sandbox</h6>
                                        <span
                                            class="badge bg-secondary-subtle text-secondary border border-secondary-subtle text-uppercase"
                                            style="font-size: 10px;">Share Planned</span>
                                    </div>
                                    <p class="text-muted mb-0" style="font-size: 0.75rem; line-height: 1.5;">
                                        Your mocks are private to this browser session. Sharing capabilities are coming
                                        soon.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="pt-4 border-top mt-4">
                        <div class="d-flex align-items-center gap-2 text-muted font-mono" style="font-size: 0.75rem;">
                            <span class="bg-success rounded-circle d-inline-block spinner-grow spinner-grow-sm"
                                  style="width: 8px; height: 8px; animation-duration: 2s;"></span>
                            SYSTEM ONLINE
                        </div>
                    </div>
                </div>

                <div
                    class="col-md-7 bg-white p-5 d-flex flex-column justify-content-center position-relative bg-grid-pattern">
                    <div v-if="!isReturningUser">
                        <h2 class="fw-bold text-dark mb-2">Start New Session</h2>
                        <p class="text-muted mb-4" style="font-size: 0.875rem;">
                            Generate a unique API key to start mocking. This session will persist in your browser until
                            the daily wipe.
                        </p>

                        <button
                            @click="handleStartNew"
                            class="btn btn-dark w-100 py-3 d-flex align-items-center justify-content-center gap-2 shadow fw-semibold">
                            Generate API Key
                            <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                      d="M17 8l4 4m0 0l-4 4m4-4H3"></path>
                            </svg>
                        </button>

                        <p class="text-center text-muted mt-4" style="font-size: 0.75rem;">
                            By continuing, you agree to the <a class="text-decoration-underline text-secondary"
                                                               href="/fair-use" target="_blank" rel="noopener noreferrer">fair use
                            policy</a>.
                        </p>
                    </div>

                    <div v-if="isReturningUser">
                        <div
                            class="d-inline-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-success-subtle text-success border border-success-subtle mb-3"
                            style="font-size: 0.75rem;">
                            <svg fill="none" height="12" stroke="currentColor" viewBox="0 0 24 24" width="12">
                                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round"
                                      stroke-linejoin="round"
                                      stroke-width="2"></path>
                            </svg>
                            <span class="fw-bold">Previous Session Found</span>
                        </div>

                        <h2 class="fw-bold text-dark mb-2">Welcome Back</h2>
                        <p class="text-muted mb-4" style="font-size: 0.875rem;">
                            We found a locally stored API key. Would you like to continue working with your existing
                            mocks?
                        </p>

                        <div class="d-flex flex-column gap-3">
                            <button
                                class="btn btn-primary w-100 py-3 fw-semibold shadow-sm d-flex align-items-center justify-content-center gap-2"
                                @click="handleContinue">
                                Continue Session
                            </button>

                            <button class="btn btn-outline-secondary w-100 py-3 fw-semibold bg-white text-dark"
                                    @click="isReturningUser = false">
                                Discard & Generate New Key
                            </button>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </div>
</template>