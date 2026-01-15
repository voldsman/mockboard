<script setup>
import {onMounted} from "vue";
import {useBoardStore} from "@/stores/boardStore.js";
import uiHelper from "@/helpers/uiHelper.js";
import {storeToRefs} from "pinia";
import {useToast} from "@/useToast.js";

const emit = defineEmits(['edit-mock'])
const boardStore = useBoardStore();
const { mockRules } = storeToRefs(boardStore)
const {success, error} = useToast()

onMounted(async () => {
    if (!boardStore.board) {
        await boardStore.restoreSession();
    }

    if (mockRules.value.length === 0) {
        await boardStore.fetchMockRules()
    }
})

const handleMockRuleEdit = (mockRuleId) => {
    emit('edit-mock', mockRuleId)
}

const handleMockRuleDelete = async (mockRuleId) => {
    if (!window.confirm(`Are you sure you want to delete mock rule?`)) return

    try {
        await boardStore.deleteMockRuleById(mockRuleId);
        success('Mock rule deleted');
    } catch (err) {
        console.error(err);
        error(`Error deleting mock rule: ${err}`);
    }
}
</script>
<template>
    <div class="card shadow-sm border-0 bg-transparent">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="m-0 fw-bold">Mock Endpoints
                <span class="badge rounded-pill bg-info" v-if="mockRules.length !== 0">
                    {{boardStore.mockUsageCount}}
                </span>
            </h6>
        </div>

        <div v-if="mockRules.length === 0" class="text-center py-5 bg-white rounded-3 border border-dashed">
            <h6 class="text-dark mb-1">Your board is empty.</h6>
            <small class="text-muted">No mock endpoints configured yet.</small>
        </div>

        <div v-else class="d-flex flex-column gap-3">
            <div v-for="mockRule in mockRules" :key="mockRule.id"
                 class="card border-0 shadow-sm hover-shadow transition-all overflow-hidden">

                <div class="card-body p-0">
                    <div class="d-flex align-items-stretch">
                        <div :class="['px-2 d-flex align-items-center justify-content-center', uiHelper.getMethodColor(mockRule.method)]"
                             style="width: 12px;">
                        </div>

                        <div class="p-3 w-100 d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3">

                            <div class="d-flex align-items-center gap-3 overflow-hidden">
                            <span :class="['badge rounded-pill px-3 py-2', uiHelper.getMethodBadge(mockRule.method)]">
                                {{ mockRule.method }}
                            </span>
                                <code class="text-dark fw-medium text-truncate fs-6" style="max-width: 400px;">
                                    {{ mockRule.path }}
                                </code>
                            </div>

                            <div class="d-flex align-items-center gap-4 ms-auto">
                                <div class="text-center">
                                    <small class="text-muted d-block text-uppercase ls-1" style="font-size: 0.65rem;">Status</small>
                                    <span class="fw-semibold text-dark small">
                                    {{ mockRule.statusCode }}
                                </span>
                                </div>

                                <div class="text-center">
                                    <small class="text-muted d-block text-uppercase ls-1" style="font-size: 0.65rem;">Delay</small>
                                    <span class="fw-semibold text-dark small">{{mockRule.delay ? mockRule.delay : 0}}ms</span>
                                </div>

                                <div class="btn-group shadow-sm rounded-3">
                                    <button
                                        @click="handleMockRuleEdit(mockRule.id)"
                                        class="btn btn-white btn-sm border-end px-3" title="Edit">
                                            <i class="bi bi-pencil-square"></i>
                                    </button>
                                    <button
                                        @click="handleMockRuleDelete(mockRule.id)"
                                        class="btn btn-white btn-sm text-danger px-3" title="Delete">
                                            <i class="bi bi-trash3"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>