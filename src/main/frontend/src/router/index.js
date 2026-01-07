import {createRouter, createWebHistory} from 'vue-router'
import BoardView from "@/views/BoardView.vue";
import ErrorView from "@/views/ErrorView.vue";
import FairUseView from "@/views/FairUseView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'board',
            component: BoardView,
        },
        {
            path: '/fair-use',
            name: 'fair-use',
            component: FairUseView,
        },
        {
            path: '/:pathMatch(.*)*',
            name: 'not-found',
            component: ErrorView
        }
    ],
})

export default router
