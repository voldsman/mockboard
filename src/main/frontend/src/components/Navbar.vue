<script setup>
import {computed, ref} from 'vue'
import {useBoardStore} from '@/stores/boardStore.js'
import {useToast} from "@/useToast.js";

const {success} = useToast()

const boardStore = useBoardStore()
const copied = ref(false)

const displayUrl = computed(() => {
    return boardStore.board?.id
        ? `${window.location.origin}/m/${boardStore.board.id}`
        : 'Session initializing...'
})

const copyUrl = () => {
    navigator.clipboard.writeText(displayUrl.value)
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
}

const closeBoard = async () => {
    if (!window.confirm("Are you sure you want to delete the board?")) return;

    try {
        await boardStore.deleteBoardById()
        boardStore.clearBoardStore()
        success('Board successfully closed. Have a nice day :) ')
        setTimeout(() => (window.location.reload()), 500)
    } catch (err) {
        boardStore.clearBoardStore()
    }
}
</script>

<template>
    <nav class="navbar navbar-fixed fixed-top px-3 bg-white">
        <div class="d-flex align-items-center w-100">
            <a href="/" class="navbar-brand me-4 fw-bold d-flex align-items-center">
                <img src="/logo.png" class="navbar-logo me-2" alt="logo" width="30" height="30"/>
                MockBoard<span class="text-muted fw-light">.dev</span>
            </a>

            <div class="input-group input-group-sm mx-auto" style="min-width: 300px; max-width: 560px;">
                <span class="input-group-text bg-light text-muted fw-bold text-xs">BASE URL</span>

                <input
                    type="text"
                    class="form-control font-mono text-muted bg-white text-center"
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