<script setup>
import { ref, onMounted } from 'vue'

const emit = defineEmits(['session-start', 'session-continue'])
const SESSION_KEY = 'mockboard_session_id'
const showModal = ref(false)
const isReturningUser = ref(false)

onMounted(() => {
  const existingSession = localStorage.getItem(SESSION_KEY)
  if (existingSession) {
    isReturningUser.value = true
  }
})

const handleStartNew = () => {
  localStorage.removeItem(SESSION_KEY)
  localStorage.setItem(SESSION_KEY, 'new-generated-id-' + Date.now())

  showModal.value = false
  emit('session-start')
}

const handleContinue = () => {
  showModal.value = false
  emit('session-continue')
}
</script>

<template>
  <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-gray-900/90 backdrop-blur-md transition-opacity"></div>

    <div
      class="relative bg-white rounded-xl shadow-2xl w-full max-w-4xl overflow-hidden flex flex-col md:flex-row min-h-[450px] border border-gray-700"
    >
      <div class="bg-gray-50 border-r border-gray-200 md:w-2/5 p-8 flex flex-col justify-between">
        <div>
          <div class="flex items-center gap-2 mb-8">
            <div
              class="w-8 h-8 bg-black rounded flex items-center justify-center text-white font-bold font-mono"
            >
              MB
            </div>
            <span class="font-bold text-gray-900 tracking-tight"
              >MockBoard<span class="text-indigo-600">.dev</span></span
            >
          </div>

          <div class="space-y-6">
            <div class="flex gap-3">
              <div class="mt-1 text-orange-500">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  ></path>
                </svg>
              </div>
              <div>
                <h3 class="font-semibold text-gray-900 text-sm">Disposable Workspace</h3>
                <p class="text-xs text-gray-500 mt-1 leading-relaxed">
                  Everything is ephemeral. All data is automatically wiped daily at
                  <span class="font-mono text-gray-700">03:00 UTC</span>.
                </p>
              </div>
            </div>

            <div class="flex gap-3">
              <div class="mt-1 text-indigo-600">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  ></path>
                </svg>
              </div>
              <div>
                <h3 class="font-semibold text-gray-900 text-sm">Strict Rate Limits</h3>
                <p class="text-xs text-gray-500 mt-1 leading-relaxed">
                  Fair use policy applies. Mock execution and creation are rate-limited per API Key.
                </p>
              </div>
            </div>

            <div class="flex gap-3">
              <div class="mt-1 text-gray-400">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                  ></path>
                </svg>
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <h3 class="font-semibold text-gray-900 text-sm">Private Sandbox</h3>
                  <span
                    class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-gray-200 text-gray-500 uppercase tracking-wide"
                    >Share Planned</span
                  >
                </div>
                <p class="text-xs text-gray-500 mt-1 leading-relaxed">
                  Your mocks are private to this browser session. Sharing capabilities are coming
                  soon.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="pt-6 border-t border-gray-200">
          <div class="flex items-center gap-2 text-xs text-gray-400 font-mono">
            <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
            SYSTEM ONLINE
          </div>
        </div>
      </div>

      <div class="bg-white md:w-3/5 p-8 flex flex-col justify-center relative">
        <div class="absolute top-0 right-0 p-6 opacity-5 pointer-events-none">
          <svg
            width="100"
            height="100"
            viewBox="0 0 100 100"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M0 0H100V100H0V0Z" fill="url(#grid-pattern)" />
            <defs>
              <pattern id="grid-pattern" width="10" height="10" patternUnits="userSpaceOnUse">
                <path d="M10 0L0 0L0 10" stroke="currentColor" stroke-width="0.5" />
              </pattern>
            </defs>
          </svg>
        </div>

        <div v-if="!isReturningUser" class="z-10">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">Start New Session</h1>
          <p class="text-gray-500 mb-8 text-sm">
            Generate a unique API key to start mocking. This session will persist in your browser
            until the daily wipe.
          </p>

          <button
            @click="handleStartNew"
            class="group w-full py-3 px-4 bg-black hover:bg-gray-800 text-white font-semibold rounded-lg shadow-lg transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            <span>Generate API Key</span>
            <svg
              class="w-4 h-4 group-hover:translate-x-1 transition-transform"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 8l4 4m0 0l-4 4m4-4H3"
              ></path>
            </svg>
          </button>

          <p class="text-center text-xs text-gray-400 mt-4">
            By continuing, you agree to the
            <a href="#" class="underline hover:text-gray-600">fair use policy</a>.
          </p>
        </div>

        <div v-else class="z-10">
          <div
            class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-green-50 text-green-700 text-xs font-medium border border-green-100 mb-4"
          >
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              ></path>
            </svg>
            Previous Session Found
          </div>

          <h1 class="text-2xl font-bold text-gray-900 mb-2">Welcome Back</h1>
          <p class="text-gray-500 mb-8 text-sm">
            We found a locally stored API key. Would you like to continue working with your existing
            mocks?
          </p>

          <div class="space-y-3">
            <button
              @click="handleContinue"
              class="w-full py-3 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg shadow-sm transition-colors flex items-center justify-center gap-2 cursor-pointer"
            >
              <span>Continue Session</span>
            </button>

            <button
              @click="handleStartNew"
              class="w-full py-3 px-4 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 font-semibold rounded-lg transition-colors text-sm cursor-pointer"
            >
              Discard & Generate New Key
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>