<script setup>
import {getConfig} from "@/config";
</script>
<template>
    <div class="fixed-top h-100 w-100 d-flex align-items-center justify-content-center bg-light" style="overflow-y: auto;">

        <div class="card border-0 shadow-sm my-4" style="width: 100%; max-width: 600px;">

            <div class="card-header bg-white border-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <img src="/logo.png" alt="logo" width="35" height="35" class="opacity-75"/>
                    <span class="fw-bold small">MockBoard.dev
                        <span class="small fw-light">({{getConfig().app.version}})</span>
                    </span>
                </div>
                <div class="badge bg-black text-white fw-normal py-2 px-3 rounded-pill">
                    AS IS
                </div>
            </div>

            <div class="card-body p-4 p-md-5">

                <div class="mb-4">
                    <h2 class="fw-bold mb-2">Fair Use & Terms</h2>
                    <p class="text-muted small">
                        I am an engineer, not a lawyer. However, by using this infrastructure, you agree to the following operational rules.
                    </p>
                </div>

                <div class="d-flex flex-column gap-3">

                    <div class="bg-light p-3 rounded-2 border border-secondary border-opacity-10">
                        <p class="mb-1 small fw-bold text-dark">
                            <i class="bi bi-radioactive me-2"></i>Ephemerality & Data Wipe
                        </p>
                        <p class="text-muted small mb-0" style="font-size: 0.85rem; line-height: 1.5;">
                            This is a sandbox, not a vault. The database undergoes a hard reset daily at <span class="badge bg-black text-white font-monospace fw-normal">03:00 UTC</span>.
                            Any data you leave here will be vaporized. Do not use this for long-term storage or critical dependencies.
                        </p>
                    </div>

                    <div class="bg-light p-3 rounded-2 border border-secondary border-opacity-10">
                        <p class="mb-1 small fw-bold text-dark">
                            <i class="bi bi-bricks me-2"></i>System Integrity & Abuse
                        </p>
                        <ul class="text-muted small mb-0 ps-3" style="font-size: 0.85rem; line-height: 1.5;">
                            <li class="mb-1"><strong>No Automation:</strong> Using scripts to bypass rate limits, creating mass boards, or rotating IPs to evade blocks is prohibited.</li>
                            <li class="mb-1"><strong>No Stress Testing:</strong> Do not use this service as a target for your load testing tools (JMeter, k6, etc.). Host it yourself via Docker for that.</li>
                            <li><strong>Result:</strong> Violators will be banned at the subnet level without warning.</li>
                        </ul>
                    </div>

                    <div class="bg-light p-3 rounded-2 border border-secondary border-opacity-10">
                        <p class="mb-1 small fw-bold text-dark">
                            <i class="bi bi-shield-x me-2"></i>Zero Warranty & Liability
                        </p>
                        <p class="text-muted small mb-2" style="font-size: 0.85rem; line-height: 1.5;">
                            The service is provided "AS IS". There are no SLAs. I do not carry a pager. If the service goes down during your client demo, that is unfortunate, but it is not my emergency.
                        </p>
                        <p class="text-muted small mb-0" style="font-size: 0.85rem; line-height: 1.5;">
                            I am not responsible for any lost profits, data leaks (due to you sharing keys), or missed deadlines.
                        </p>
                    </div>

                </div>

                <div class="d-grid gap-3 mt-4 pt-2">
                    <router-link to="/" class="btn btn-dark py-3 fw-semibold shadow-sm">
                        I Understand, Take Me Back
                    </router-link>

                    <div class="text-center">
                        <a href="https://github.com/voldpix/mockboard" target="_blank" class="text-muted small text-decoration-underline">
                            Wait, I want to host it myself (GitHub)
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>