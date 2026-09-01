const api = {
  async getTickets() {
    const response = await fetch('/api/tickets')

    if (!response.ok) {
      throw new Error('Could not load tickets')
    }

    return response.json()
  },

  async getDashboard() {
    const response = await fetch('/api/dashboard')

    if (!response.ok) {
      throw new Error('Could not load dashboard')
    }

    return response.json()
  },

  async createTicket(ticket) {
    const response = await fetch('/api/tickets', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(ticket)
    })

    if (!response.ok) {
      throw new Error('Could not create ticket')
    }

    return response.json()
  }
}

export default api