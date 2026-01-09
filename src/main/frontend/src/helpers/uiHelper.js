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

export default {
    getMethodColor,
    getMethodBadge,
}