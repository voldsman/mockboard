const getMethodColor = (method) => {
    const colors = {
        'GET': 'bg-success',
        'POST': 'bg-primary',
        'PUT': 'bg-warning',
        'DELETE': 'bg-danger'
    }
    return colors[method] || 'bg-secondary'
}

const getMethodBadge = (method) => {
    const colors = {
        'GET': 'bg-success-subtle text-success',
        'POST': 'bg-primary-subtle text-primary',
        'PUT': 'bg-warning-subtle text-warning',
        'DELETE': 'bg-danger-subtle text-danger',
    }
    return colors[method] || 'bg-light text-dark'
}

const formatWebhookTime = (timestamp) => {
    const now = new Date()
    const date = new Date(timestamp)
    const diffInMinutes = Math.floor((now - date) / 60_000)

    if (diffInMinutes < 1) return 'Just Now'
    if (diffInMinutes < 30) return `${diffInMinutes}m ago`
    return date.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    })
}

export default {
    getMethodColor,
    getMethodBadge,
    formatWebhookTime
}