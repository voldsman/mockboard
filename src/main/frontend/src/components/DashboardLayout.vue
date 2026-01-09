<script setup>
import {ref} from 'vue'
import constants from '@/constants.js'
import DashboardStats from "@/components/DashboardStats.vue";
import CreateMockEndpoint from "@/components/CreateMockEndpoint.vue";
import DashboardMockRules from "@/components/DashboardMockRules.vue";

const viewDashboard = constants.DASHBOARD_VIEWS.DASHBOARD
const viewCreateMock = constants.DASHBOARD_VIEWS.CREATE_MOCK
const viewEditMock = constants.DASHBOARD_VIEWS.EDIT_MOCK
const viewLogDetails = constants.DASHBOARD_VIEWS.LOG_DETAILS

const currentView = ref(viewDashboard)
const selectedLog = ref(null)

const openCreate = () => {
    currentView.value = viewCreateMock
}

const closePanel = () => {
    currentView.value = viewDashboard
    selectedLog.value = null
}

const openLogDetails = (log) => {
    selectedLog.value = log
    currentView.value = viewLogDetails
}

defineExpose({openLogDetails})
</script>

<template>
    <main class="main-content">

        <div v-if="currentView === viewDashboard">
            <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
                <div class="d-flex align-items-center gap-3">
                    <h4 class="fw-bold mb-0 text-dark">Dashboard</h4>
                </div>
                <div class="d-flex gap-2">
                    <button
                        @click="openCreate"
                        type="button"
                        class="btn btn-outline-primary px-4 fw-bold">Add Mock Endpoint</button>
                </div>
            </div>

<!--            <DashboardStats />-->
            <DashboardMockRules />
        </div>

        <div v-if="currentView === viewCreateMock" class="w-100 px-lg-4">
            <CreateMockEndpoint
            @close="currentView = viewDashboard"/>
        </div>

        <!-- NOT NEEDED FOR NOW d-none-->
<!--        <div id="view-log-detail-1" class="view-section d-none">-->
<!--            <div class="d-flex justify-content-between align-items-center mb-4">-->
<!--                <div>-->
<!--                    <h4 class="mb-1">Request Details</h4>-->
<!--                    <span class="text-muted font-mono text-sm">ID: req_8237482374</span>-->
<!--                </div>-->
<!--                <div class="d-flex gap-2">-->
<!--                    <button class="btn btn-outline-secondary" onclick="showView('dashboard')">Close</button>-->
<!--                    <button class="btn btn-primary" onclick="showView('create-mock')">-->
<!--                        <i class="bi bi-magic"></i> Create Mock from Request-->
<!--                    </button>-->
<!--                </div>-->
<!--            </div>-->

<!--            <div class="card shadow-sm border-0 mb-4">-->
<!--                <div class="card-body">-->
<!--                    <div class="row align-items-center">-->
<!--                        <div class="col-md-1">-->
<!--                            <span class="badge bg-success fs-6 w-100 py-2">POST</span>-->
<!--                        </div>-->
<!--                        <div class="col-md-7 border-end">-->
<!--                            <div class="font-mono fs-5">/api/v1/auth/login</div>-->
<!--                        </div>-->
<!--                        <div class="col-md-2 border-end text-center">-->
<!--                            <div class="text-muted text-xs text-uppercase fw-bold">Status</div>-->
<!--                            <div class="text-success fw-bold">200 OK</div>-->
<!--                        </div>-->
<!--                        <div class="col-md-2 text-center">-->
<!--                            <div class="text-muted text-xs text-uppercase fw-bold">Duration</div>-->
<!--                            <div>45ms</div>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                </div>-->
<!--            </div>-->

<!--            <div class="row">-->
<!--                <div class="col-md-6">-->
<!--                    <div class="card shadow-sm border-0 h-100">-->
<!--                        <div class="card-header bg-light fw-bold">Request</div>-->
<!--                        <div class="card-body p-0">-->
<!--                            <nav>-->
<!--                                <div class="nav nav-tabs nav-justified" id="nav-tab" role="tablist">-->
<!--                                    <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#req-headers">-->
<!--                                        Headers-->
<!--                                    </button>-->
<!--                                    <button class="nav-link" data-bs-toggle="tab" data-bs-target="#req-body">Body-->
<!--                                    </button>-->
<!--                                    <button class="nav-link" data-bs-toggle="tab" data-bs-target="#req-params">Params-->
<!--                                    </button>-->
<!--                                </div>-->
<!--                            </nav>-->
<!--                            <div class="tab-content p-3">-->
<!--                                <div class="tab-pane fade show active" id="req-headers">-->
<!--                                    <table class="table table-sm table-borderless font-mono text-sm">-->
<!--                                        <tr>-->
<!--                                            <td class="text-muted text-end pe-3">Host:</td>-->
<!--                                            <td>localhost:8000</td>-->
<!--                                        </tr>-->
<!--                                        <tr>-->
<!--                                            <td class="text-muted text-end pe-3">User-Agent:</td>-->
<!--                                            <td>PostmanRuntime/7.29.0</td>-->
<!--                                        </tr>-->
<!--                                        <tr>-->
<!--                                            <td class="text-muted text-end pe-3">Accept:</td>-->
<!--                                            <td>*/*</td>-->
<!--                                        </tr>-->
<!--                                        <tr>-->
<!--                                            <td class="text-muted text-end pe-3">Content-Type:</td>-->
<!--                                            <td>application/json</td>-->
<!--                                        </tr>-->
<!--                                    </table>-->
<!--                                </div>-->
<!--                                <div class="tab-pane fade" id="req-body">-->
<!--                                    <pre class="bg-dark text-success p-3 rounded font-mono m-0"-->
<!--                                         style="font-size: 12px;">{-->
<!--    "username": "admin",-->
<!--    "password": "***"-->
<!--}</pre>-->
<!--                                </div>-->
<!--                                <div class="tab-pane fade" id="req-params">-->
<!--                                    <div class="text-muted text-center py-3">No query parameters</div>-->
<!--                                </div>-->
<!--                            </div>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                </div>-->

<!--                <div class="col-md-6">-->
<!--                    <div class="card shadow-sm border-0 h-100">-->
<!--                        <div class="card-header bg-light fw-bold">Response</div>-->
<!--                        <div class="card-body p-0">-->
<!--                            <div class="p-3">-->
<!--                                <h6 class="text-muted text-xs text-uppercase mb-2">Body Payload</h6>-->
<!--                                <pre class="bg-dark text-success p-3 rounded font-mono m-0" style="font-size: 12px;">{-->
<!--    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",-->
<!--    "expires_in": 3600-->
<!--}</pre>-->
<!--                            </div>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                </div>-->
<!--            </div>-->
<!--        </div>-->
    </main>
</template>

<style scoped>

</style>
