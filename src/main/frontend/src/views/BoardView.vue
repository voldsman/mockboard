<script setup>
import Navbar from '@/components/Navbar.vue'
import SessionOverlay from '@/components/SessionOverlay.vue'
import RequestSidebar from '@/components/RequestSidebar.vue'
import {ref} from 'vue'
import DashboardLayout from '@/components/DashboardLayout.vue'
import Footer from "@/components/Footer.vue";

const dashboardRef = ref(null)
const isReady = ref(false)

const onSessionStart = () => {
    console.log('Starting new session')
    isReady.value = true
}

const onSessionContinue = () => {
    console.log('Continue continue')
    isReady.value = true
}

const onLogSelected = (log) => {
    if (dashboardRef.value) {
        dashboardRef.value.openWebhookDetails(log)
    }
}
</script>

<template>
    <SessionOverlay
        v-if="!isReady"
        @session-start="onSessionStart"
        @session-continue="onSessionContinue"/>

    <template v-if="isReady">
        <Navbar/>
        <RequestSidebar @view-webhook="onLogSelected"/>
        <DashboardLayout ref="dashboardRef"/>
        <Footer/>
    </template>
</template>
