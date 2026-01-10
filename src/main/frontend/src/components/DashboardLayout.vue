<script setup>
import {ref} from 'vue'
import constants from '@/constants.js'
import DashboardMockRules from "@/components/DashboardMockRules.vue";
import {useBoardStore} from "@/stores/boardStore.js";
import MockEndpointForm from "@/components/MockEndpointForm.vue";

const boardStore = useBoardStore();

const viewDashboard = constants.DASHBOARD_VIEWS.DASHBOARD
const viewCreateMock = constants.DASHBOARD_VIEWS.CREATE_MOCK
const viewEditMock = constants.DASHBOARD_VIEWS.EDIT_MOCK
const viewLogDetails = constants.DASHBOARD_VIEWS.LOG_DETAILS

const currentView = ref(viewDashboard)
const selectedLog = ref(null)
const selectedMockRuleId = ref(null)

const openCreate = () => {
    selectedMockRuleId.value = null
    currentView.value = viewCreateMock
}

const openEdit = (mockRuleId) => {
    selectedMockRuleId.value = mockRuleId
    currentView.value = viewEditMock
}

const closePanel = () => {
    currentView.value = viewDashboard
    selectedLog.value = null
}

const openLogDetails = (log) => {
    selectedLog.value = log
    currentView.value = viewLogDetails
}

defineExpose({openLogDetails, openEdit})
</script>

<template>
    <main class="main-content">

        <div v-if="currentView === viewDashboard">
            <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
                <div class="d-flex align-items-center gap-3">
                    <h4 class="fw-bold mb-0 text-dark">Dashboard</h4>
                </div>
                <div
                    v-if="boardStore.canAddMoreMocks"
                    class="d-flex gap-2">
                    <button
                        @click="openCreate"
                        type="button"
                        class="btn btn-outline-primary px-4 fw-bold">Add Mock Endpoint</button>
                </div>
            </div>

<!--            <DashboardStats />-->
            <DashboardMockRules @edit-mock="openEdit"/>
        </div>

        <div v-if="currentView === viewCreateMock" class="w-100 px-lg-4">
            <MockEndpointForm mode="create" @close="closePanel"/>
        </div>

        <div v-if="currentView === viewEditMock" class="w-100 px-lg-4">
            <MockEndpointForm mode="edit" :mock-rule-id="selectedMockRuleId" @close="closePanel"/>
        </div>
    </main>
</template>

<style scoped>

</style>
