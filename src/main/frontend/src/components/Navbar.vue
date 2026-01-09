<script setup>
import {computed, ref} from 'vue'
import constants from '@/constants'
import {useBoardStore} from '@/stores/boardStore.js'
import {useToast} from "@/useToast.js";

const {success} = useToast()

const boardStore = useBoardStore()
const copied = ref(false)

const displayUrl = computed(() => {
    return boardStore.board.apiKey
        ? `${constants.SERVER_URL}/m/${boardStore.board.apiKey}`
        : 'Session initializing...'
})

const copyUrl = () => {
    navigator.clipboard.writeText(displayUrl.value)
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
}

const closeBoard = () => {
    const result = confirm("Are you sure you want to delete the board?")
    if (result) {
        boardStore.clearBoardStore()
        success('Board successfully closed. Have a nice day :) ')
        setTimeout(() => (window.location.reload()), 500)

        // todo: add call to server
    }
}
</script>

<template>
    <nav class="navbar navbar-fixed fixed-top px-3">
        <div class="d-flex align-items-center w-100">
            <a class="navbar-brand me-4 fw-bold">
                <i class="bi bi-box-seam-fill text-primary"></i> MockBoard<span class="text-muted fw-light">.dev</span>
            </a>

            <div class="input-group input-group-sm mx-auto" style="min-width: 300px; max-width: 500px;">
                <span class="input-group-text bg-light text-muted fw-bold text-xs">BASE URL</span>

                <input
                    type="text"
                    class="form-control font-mono text-muted bg-white"
                    :class="{ 'border-success border-2 shadow-none': copied }"
                    :value="displayUrl" readonly>
                <button
                    @click="copyUrl"
                    class="btn"
                    :class="copied ? 'btn-success' : 'btn-outline-secondary'"
                    type="button"
                    title="Copy">
                    <i class="bi bi-clipboard"></i>
                </button>
            </div>

            <div class="d-flex align-items-center gap-3">
                <button
                    @click="closeBoard"
                    class="btn btn-sm btn-outline-danger">Close board
                </button>
            </div>
        </div>
    </nav>
</template>