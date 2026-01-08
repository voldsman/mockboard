<script setup>
import {onMounted, ref} from "vue";
import {useBoardStore} from "@/stores/boardStore.js";
import boardService from "@/services/boardService.js";
import {MockRuleModel} from "@/models/mockRuleModel.js";
import {getReasonPhrase} from 'http-status-codes';

const boardStore = useBoardStore();
const mockRules = ref([])
onMounted(async () => {
    const storeBoardModel = boardStore.getBoard
    const storeMockRules = boardStore.getMockRules

    if (storeMockRules.length === 0) {
        try {
            const result = await boardService.getMockRules(storeBoardModel.id, storeBoardModel.ownerToken);
            result.data.forEach((mr) => {
                mockRules.value.push(new MockRuleModel(mr))
            })
            boardStore.mockRules = result.data.map(mr => new MockRuleModel(mr));
        } catch (error) {
            console.error('Error getting mock rules', error);
        }
    } else {
        mockRules.value = storeMockRules;
    }
})

const getStatusDetails = (code) => {
    let standardText = "Unknown";
    try {
        standardText = getReasonPhrase(code)
    } catch (error) {
        console.error('Error extracting status code text', error);
    }

    const firstDigit = Math.floor(code / 100);
    const styleMap = {
        2: "bg-success-subtle text-success border-success-subtle",
        3: "bg-info-subtle text-info border-info-subtle",
        4: "bg-warning-subtle text-warning border-warning-subtle",
        5: "bg-danger-subtle text-danger border-danger-subtle",
    };

    return {
        fullText: `${code} ${standardText}`,
        cssClass: styleMap[firstDigit] || "bg-light text-dark border-secondary-subtle"
    };
};
</script>
<template>
    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
            <h6 class="m-0 fw-bold">Mock Endpoints</h6>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="bg-light">
                <tr>
                    <th class="ps-4">Method</th>
                    <th>Path Pattern</th>
                    <th>Status</th>
                    <th>Delay</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="mockRule in mockRules" :key="mockRule.id">
                    <td class="ps-4">
                        <span class="badge bg-success">
                            {{mockRule.method}}
                        </span>
                    </td>
                    <td class="font-mono text-truncate" style="max-width: 250px;">
                        {{mockRule.path}}
                    </td>
                    <td>
                        <span :class="['badge border', getStatusDetails(mockRule.statusCode).cssClass]">
                            {{ getStatusDetails(mockRule.statusCode).fullText }}
                        </span>
                    </td>
                    <td class="text-muted">0ms TODO;</td>
                    <td>
                        <button class="btn btn-sm btn-light border"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-light border text-danger"><i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>