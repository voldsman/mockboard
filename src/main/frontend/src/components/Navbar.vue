<script setup>
import {computed, ref} from 'vue'
import constants from '@/constants'

const mockApiKey = ref('EUVFAnUCrV3R7gz7')

const executionUrl = computed(() => `${constants.SERVER_URL}/m/${mockApiKey.value}/`)

const copied = ref(false)
const copyUrl = () => {
    navigator.clipboard.writeText(executionUrl.value)
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
}

const closeBoard = () => {
    alert('Close board clicked')
}
</script>

<template>
    <nav
        class="h-16 bg-white border-b border-gray-200 px-6 flex items-center justify-between fixed top-0 left-0 right-0 z-50">
        <div class="flex items-center w-1/4">
            <div class="flex items-center gap-2">
                <div
                    class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center text-white font-bold text-lg">
                    MB
                </div>
                <span class="text-xl font-bold tracking-tight text-gray-900">
                    MockBoard<span class="text-indigo-600">.dev</span>
                </span>
            </div>
        </div>

        <div class="flex-1 flex justify-center max-w-2xl">
            <div class="relative w-full max-w-lg group">
                <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <span class="text-gray-600 text-sm font-medium">URL</span>
                </div>

                <input
                    :value="executionUrl"
                    class="block w-full pl-20 pr-12 py-2 bg-gray-50 border border-gray-200 text-gray-600 text-sm rounded-md focus:ring-0 focus:border-gray-300 disabled:opacity-75 disabled:cursor-not-allowed font-mono shadow-sm"
                    disabled
                    readonly
                    type="text"
                />

                <button
                    class="absolute inset-y-1 right-1 px-3 flex items-center bg-white border border-gray-200 hover:bg-gray-50 text-gray-500 rounded text-xs font-medium transition-colors cursor-pointer"
                    title="Copy URL"
                    @click="copyUrl"
                >
                    <span v-if="!copied">Copy</span>
                    <span v-else class="text-green-600">Copied!</span>
                </button>
            </div>
        </div>

        <div class="flex items-center justify-end w-1/4 gap-4">
            <div class="flex items-center gap-2 px-3 py-1 bg-green-50 rounded-full border border-green-100">
        <span class="relative flex h-2.5 w-2.5">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500"></span>
        </span>
                <span class="text-xs font-medium text-green-700 uppercase tracking-wide">Connected</span>
            </div>

            <button
                class="text-gray-400 hover:text-red-500 transition-colors p-2 rounded-md hover:bg-red-50 cursor-pointer"
                title="Close Board"
                @click="closeBoard"
            >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
        </div>

    </nav>
</template>