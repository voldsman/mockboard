import {defineStore} from 'pinia'

export const useBoardStore = defineStore("boardStore", {
  state: () => {
    return {
      boardId: '',
      apiKey: ''
    }
  }
})
