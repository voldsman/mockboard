import {reactive} from "vue";

export const selectionState = reactive({
    activeId: null,
    select(id) {
        this.activeId = id
    },
    clear() {
        this.activeId = null
    }
})