<script setup>
import { ref } from 'vue'
import constants from '@/constants.js'

const mocks = ref([])

const currentView = ref('dashboard') // 'dashboard', 'create-mock', 'edit-mock', log-details'
const selectedLog = ref(null)

const openCreate = () => {
  currentView.value = 'create'
}

const closePanel = () => {
  currentView.value = 'dashboard'
  selectedLog.value = null
}

const openLogDetails = (log) => {
  selectedLog.value = log
  currentView.value = 'log-details'
}

defineExpose({ openLogDetails })
</script>

<template>
  <div class="pl-72 pt-14 min-h-screen bg-gray-50/30">
    <div v-if="currentView === 'dashboard'" class="p-8 max-w-7xl mx-auto">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Your Mocks</h1>
          <p class="text-sm text-gray-500">Manage your active endpoints.</p>
        </div>
        <button
          @click="openCreate"
          :disabled="mocks.length >= constants.MAX_MOCKS"
          class="bg-black text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-800 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 4v16m8-8H4"
            ></path>
          </svg>
          Create Mock
        </button>
      </div>

      <div
        v-if="mocks.length === 0"
        class="border-2 border-dashed border-gray-300 rounded-xl p-12 text-center bg-gray-50"
      >
        <div
          class="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-400"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
            ></path>
          </svg>
        </div>
        <h3 class="text-gray-900 font-medium mb-1">No mocks defined</h3>
        <p class="text-gray-500 text-sm mb-6">Create your first endpoint to start testing.</p>
        <button
          @click="openCreate"
          class="text-indigo-600 font-medium text-sm hover:underline cursor-pointer"
        >
          + Create new endpoint
        </button>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"></div>
    </div>

    <div v-else-if="currentView === 'create'"
      class="p-8 max-w-6xl mx-auto animate-in fade-in slide-in-from-bottom-2 duration-200"
    >
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div
          class="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50"
        >
          <h2 class="font-bold text-gray-800">Create New Mock</h2>
          <button
            @click="closePanel"
            class="text-gray-400 hover:text-gray-600 transition p-1 cursor-pointer"
          >
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

        <div class="p-6 space-y-6">
          <div class="grid grid-cols-4 gap-4">
            <div class="col-span-1">
              <label class="block text-xs font-semibold text-gray-500 uppercase mb-2">Method</label>
              <select
                class="w-full bg-gray-50 border border-gray-200 rounded-md px-3 py-2 text-sm focus:outline-none focus:border-indigo-500"
              >
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>DELETE</option>
                <option>PATCH</option>
              </select>
            </div>
            <div class="col-span-3">
              <label class="block text-xs font-semibold text-gray-500 uppercase mb-2"
                >Endpoint Path</label
              >
              <div class="relative">
                <span class="absolute left-3 top-2 text-gray-400 text-sm">/</span>
                <input
                  type="text"
                  placeholder="api/v1/users"
                  class="w-full pl-6 bg-gray-50 border border-gray-200 rounded-md px-3 py-2 text-sm focus:outline-none focus:border-indigo-500 font-mono"
                />
              </div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase mb-2"
              >Response Body (JSON)</label
            >
            <textarea
              rows="8"
              class="w-full bg-gray-900 text-green-400 font-mono text-xs p-4 rounded-md focus:outline-none"
              placeholder='{ "message": "hello world" }'
            ></textarea>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              @click="closePanel"
              class="px-4 py-2 text-gray-600 text-sm hover:bg-gray-100 rounded-md transition cursor-pointer"
            >
              Cancel
            </button>
            <button
              class="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 shadow-sm transition cursor-pointer"
            >
              Create Mock
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="currentView === 'log-details'"
      class="p-8 max-w-6xl mx-auto animate-in fade-in slide-in-from-bottom-2 duration-200"
    >
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <div
          class="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50"
        >
          <div class="flex items-center gap-3">
            <span
              class="px-2 py-1 bg-green-100 text-green-700 text-xs font-bold rounded border border-green-200"
              >{{ selectedLog?.status }}</span
            >
            <h2 class="font-bold text-gray-800 font-mono text-sm">
              {{ selectedLog?.method }} {{ selectedLog?.path }}
            </h2>
          </div>
          <button
            @click="closePanel"
            class="text-gray-400 hover:text-gray-600 transition p-1 cursor-pointer"
          >
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
        <div class="p-12 text-center text-gray-400">
          (Request Headers and Body details would appear here)
        </div>
      </div>
    </div>
  </div>
</template>
