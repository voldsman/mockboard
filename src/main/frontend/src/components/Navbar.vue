<script setup>
import { computed, ref } from 'vue'
import constants from '@/constants'
import { useBoardStore } from '@/stores/boardStore.js'

const boardStore = useBoardStore()
const copied = ref(false)

const displayUrl = computed(() => {
  return boardStore.apiKey
    ? `${constants.SERVER_URL}/m/${boardStore.apiKey}`
    : 'Session initializing...'
})

const copyUrl = () => {
  navigator.clipboard.writeText(displayUrl.value)
  copied.value = true
  setTimeout(() => (copied.value = false), 2000)
}
</script>

<template>
  <nav
    class="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 fixed top-0 left-0 right-0 z-40"
  >
    <div class="flex items-center gap-3 w-64">
      <div
        class="w-8 h-8 bg-black rounded flex items-center justify-center text-white font-bold font-mono text-sm"
      >
        MB
      </div>
      <span class="font-bold text-gray-900 tracking-tight text-sm"
        >MockBoard<span class="text-indigo-600">.dev</span></span
      >
    </div>

    <div class="flex-1 max-w-2xl px-4">
      <div
        class="flex items-center bg-gray-50 border border-gray-200 rounded-md p-1 focus-within:ring-2 focus-within:ring-indigo-100 transition-all"
      >
        <span class="pl-3 text-gray-400 text-xs font-bold uppercase tracking-wider select-none"
          >Base URL</span
        >
        <input
          readonly
          :value="displayUrl"
          class="flex-1 bg-transparent border-none text-xs font-mono text-gray-600 px-3 focus:ring-0 w-full"
        />
        <button
          @click="copyUrl"
          class="p-1.5 rounded hover:bg-white hover:shadow-sm text-gray-400 hover:text-indigo-600 transition-all cursor-pointer"
          title="Copy to clipboard"
        >
          <span v-if="!copied">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
              ></path>
            </svg>
          </span>
          <span v-else class="text-green-600 font-bold text-xs px-1">OK</span>
        </button>
      </div>
    </div>

    <div class="flex items-center justify-end gap-4 w-64">
      <div class="hidden md:flex flex-col items-end">
        <span class="text-[10px] uppercase text-gray-400 font-bold tracking-wider">Reset In</span>
        <span class="text-xs font-mono text-gray-700">11:42:05</span>
      </div>
      <div class="h-8 w-[1px] bg-gray-200 mx-1"></div>
      <div class="flex items-center gap-2">
        <span class="relative flex h-2.5 w-2.5">
          <span
            class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"
          ></span>
          <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500"></span>
        </span>
        <span class="text-xs font-bold text-gray-600">Live</span>
      </div>
      <div class="h-8 w-[1px] bg-gray-200 mx-1"></div>
      <button class="text-gray-400 hover:text-gray-600 transition p-1 cursor-pointer"
      title="Close board">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          ></path>
        </svg>
      </button>
    </div>
  </nav>
</template>