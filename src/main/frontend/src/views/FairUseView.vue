<script setup>
import constants from '@/constants.js'
</script>

<template>
    <div class="min-vh-100 bg-light py-5">
        <div class="container" style="max-width: 800px;">
            <div class="text-center mb-5">
                <div class="d-flex align-items-center justify-content-center gap-2 mb-3 fw-bold fs-2">
                    <i class="bi bi-box-seam-fill text-primary"></i>
                    <span>MockBoard<span class="text-muted fw-light">.dev</span></span>
                </div>
                <h1 class="fw-bold mb-2">Fair Use Policy</h1>
                <p class="text-muted">Last Updated: January 2025</p>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">Service Overview</h2>
                    <p class="text-muted mb-0">
                        MockBoard.dev is a <strong>free, disposable HTTP mocking service</strong> provided as-is for development and testing purposes.
                        The service is hosted and maintained independently without any commercial backing or guarantees.
                    </p>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">Usage Limits</h2>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="p-3 bg-light rounded">
                                <div class="text-primary fw-bold mb-1">Mock Endpoints</div>
                                <div class="text-muted small">Maximum 12 per board</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="p-3 bg-light rounded">
                                <div class="text-primary fw-bold mb-1">Webhook Logs</div>
                                <div class="text-muted small">Recent 20 stored (unlimited sends)</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="p-3 bg-light rounded">
                                <div class="text-primary fw-bold mb-1">Board Creation</div>
                                <div class="text-muted small">Max 5 boards per hour per IP</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="p-3 bg-light rounded">
                                <div class="text-primary fw-bold mb-1">Webhook Execution</div>
                                <div class="text-muted small">Max 20 requests per minute</div>
                            </div>
                        </div>
                    </div>
                    <div class="alert alert-warning mt-3 mb-0 d-flex align-items-start gap-2">
                        <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
                        <div class="small">
                            Additional rate limits may be applied at any time to ensure fair usage and service stability.
                        </div>
                    </div>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">Data & Privacy</h2>
                    <ul class="mb-0 text-muted">
                        <li class="mb-2">
                            <strong>Automatic Data Deletion:</strong> All boards, mocks, and logs are automatically wiped daily at <span class="font-mono text-dark">03:00 UTC</span>.
                        </li>
                        <li class="mb-2">
                            <strong>Storage:</strong> Data is stored in MongoDB for operational purposes only.
                        </li>
                        <li class="mb-2">
                            <strong>No User Accounts:</strong> The service does not require registration or collect personal information.
                        </li>
                        <li class="mb-2">
                            <strong>Session Storage:</strong> Board credentials are stored locally in your browser.
                        </li>
                        <li class="mb-0">
                            <strong>Request Logging:</strong> IP addresses and request metadata may be logged for rate limiting and abuse prevention.
                        </li>
                    </ul>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">Disclaimer & Liability</h2>
                    <p class="text-muted mb-2">
                        <strong>This service is provided "AS IS" without any warranties or guarantees.</strong>
                    </p>
                    <ul class="mb-0 text-muted">
                        <li class="mb-2">We do not guarantee service availability, uptime, or data persistence.</li>
                        <li class="mb-2">We are not responsible for any data loss, service interruptions, or security breaches.</li>
                        <li class="mb-2">The service may be discontinued, modified, or shut down at any time without notice.</li>
                        <li class="mb-2">Do not use this service for production workloads or storing sensitive data.</li>
                        <li class="mb-0">Use at your own risk. We accept no liability for any damages or losses.</li>
                    </ul>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">Prohibited Use</h2>
                    <p class="text-muted mb-2">The following activities are strictly prohibited:</p>
                    <ul class="mb-0 text-muted">
                        <li class="mb-2">Attempting to circumvent rate limits or abuse the service</li>
                        <li class="mb-2">Using the service for illegal activities or malicious purposes</li>
                        <li class="mb-2">Storing sensitive, confidential, or personal data</li>
                        <li class="mb-2">Overloading the service with excessive requests</li>
                        <li class="mb-0">Reverse engineering or attempting to exploit vulnerabilities</li>
                    </ul>
                    <div class="alert alert-danger mt-3 mb-0 d-flex align-items-start gap-2">
                        <i class="bi bi-shield-fill-exclamation flex-shrink-0 mt-1"></i>
                        <div class="small">
                            Violation of these terms may result in immediate IP blocking and service denial.
                        </div>
                    </div>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">
                        Open Source & Self-Hosting
                    </h2>
                    <p class="text-muted mb-3">
                        MockBoard.dev is <strong>fully open source</strong> and available for self-hosting.
                        You can run your own instance locally or on your infrastructure with complete control over your data.
                    </p>
                    <div class="alert alert-info mb-3 d-flex align-items-start gap-2">
                        <i class="bi bi-info-circle-fill flex-shrink-0 mt-1"></i>
                        <div class="small">
                            <strong>Self-Host with Docker:</strong> A docker-compose configuration is included for easy local deployment.
                            Perfect for teams needing private instances or air-gapped environments.
                        </div>
                    </div>
                    <ul class="mb-0 text-muted">
                        <li class="mb-2">
                            <strong>Full Source Access:</strong> Review, modify, and deploy the code as needed
                        </li>
                        <li class="mb-2">
                            <strong>No Vendor Lock-in:</strong> Run it anywhere - your machine, cloud, or on-premises
                        </li>
                        <li class="mb-0">
                            <strong>Community Contributions:</strong> Pull requests and issues welcome
                        </li>
                    </ul>
                </div>
            </div>

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h2 class="h5 fw-bold mb-3">Contact & Attribution</h2>
                    <div class="d-flex flex-column gap-2 text-muted">
                        <div>
                            <strong>Developer:</strong>
                            <a href="https://github.com/voldpix" target="_blank" rel="noopener noreferrer" class="text-primary text-decoration-none ms-1">
                                voldpix <i class="bi bi-box-arrow-up-right small"></i>
                            </a>
                        </div>
                        <div>
                            <strong>Source Code:</strong>
                            <a href="https://github.com/voldpix/mockboard" target="_blank" rel="noopener noreferrer" class="text-primary text-decoration-none ms-1">
                                GitHub <i class="bi bi-box-arrow-up-right small"></i>
                            </a>
                        </div>
                        <div>
                            <strong>License:</strong> MIT
                        </div>
                        <div>
                            <strong>Location:</strong> USA
                        </div>
                        <div>
                            <strong>Version:</strong> {{ constants.APP_VERSION }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>