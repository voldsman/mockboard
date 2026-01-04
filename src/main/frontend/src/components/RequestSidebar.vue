<script setup>
import { ref } from 'vue'

const emit = defineEmits(['view-log'])

const logs = ref([
  { id: 1, method: 'POST', path: '/api/v1/login', status: 200, time: '14:20:01' },
  { id: 2, method: 'GET', path: '/users/123', status: 404, time: '14:19:55' },
  { id: 3, method: 'GET', path: '/products', status: 200, time: '14:18:22' },
  { id: 4, method: 'PUT', path: '/settings', status: 500, time: '14:15:10' },
])

const handleLogClick = (log) => {
  emit('view-log', log)
}
</script>

<template>
  <aside
    class="w-72 bg-white border-r border-gray-200 flex flex-col fixed top-14 bottom-0 left-0 z-30"
  >
    <div class="h-10 border-b border-gray-100 flex items-center px-4 bg-gray-50/50">
      <h3 class="text-xs font-bold text-gray-500 uppercase tracking-wider">Incoming Requests</h3>
      <span class="ml-auto text-[10px] bg-gray-200 text-gray-600 px-1.5 py-0.5 rounded-full"
        >{{ logs.length }}/20</span
      >
    </div>

    <div class="flex-1 overflow-y-auto p-2 space-y-1">
      <div
        v-for="log in logs"
        :key="log.id"
        @click="handleLogClick(log)"
        class="group flex flex-col gap-1 p-3 rounded-lg hover:bg-gray-50 cursor-pointer border border-transparent hover:border-gray-200 transition-all"
      >
        <div class="flex items-center gap-2">
          <span
            class="text-[10px] font-bold px-1.5 rounded border"
            :class="{
              'bg-green-50 text-green-700 border-green-100': log.status >= 200 && log.status < 300,
              'bg-red-50 text-red-700 border-red-100': log.status >= 400 && log.status < 500,
              'bg-orange-50 text-orange-700 border-orange-100': log.status >= 500,
            }"
          >
            {{ log.status }}
          </span>
          <span class="text-xs font-bold text-gray-700 w-10">{{ log.method }}</span>
          <span class="text-xs text-gray-600 font-mono truncate" :title="log.path">{{
            log.path
          }}</span>
        </div>
        <div class="flex items-center justify-end">
          <span class="text-[10px] text-gray-400 font-mono">{{ log.time }}</span>
        </div>
        <div class="h-0.5 w-full bg-gray-200 mx-1"></div>
      </div>

      <div v-if="logs.length === 0" class="text-center py-10 px-4">
        <p class="text-xs text-gray-400">No requests received yet.</p>
        <p class="text-[10px] text-gray-300 mt-1">Hit your Base URL to see logs here.</p>
      </div>
    </div>
  </aside>
</template>