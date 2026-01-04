<script setup>
import Navbar from '@/components/Navbar.vue'
import SessionModal from '@/components/SessionModal.vue'
import RequestSidebar from '@/components/RequestSidebar.vue'
import { useBoardStore } from '@/stores/boardStore.js'
import { ref } from 'vue'
import DashboardLayout from '@/components/DashboardLayout.vue'

const boardStore = useBoardStore()
const dashboardRef = ref(null)

const onSessionStart = () => {
  console.log('Starting new session')
}

const onSessionContinue = () => {
  console.log('Continue continue')
}

const onLogSelected = (log) => {
  if (dashboardRef.value) {
    dashboardRef.value.openLogDetails(log)
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <SessionModal @session-start="onSessionStart" @session-continue="onSessionContinue" />

    <Navbar />

    <RequestSidebar @view-log="onLogSelected" />
    <DashboardLayout ref="dashboardRef" />
  </div>
</template>
