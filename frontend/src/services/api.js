const apiBaseUrl = import.meta.env.VITE_API_URL || 'https://e-support-hub.onrender.com'

const api = {
  async getTickets() {
    const response = await fetch(`${apiBaseUrl}/api/tickets`)

    if (!response.ok) {
      throw new Error('Could not load tickets')
    }

    return response.json()
  },

  async getDashboard() {
    try {
      const url = `${apiBaseUrl}/api/dashboard`

      console.log('Calling dashboard API:', url)

      const response = await fetch(url)

      console.log('Dashboard status:', response.status)

      if (!response.ok) {
        const errorText = await response.text()

        console.error('Dashboard API response:', errorText)

        throw new Error(
            `Dashboard API returned ${response.status} ${response.statusText}`
        )
      }

      const data = await response.json()

      console.log('Dashboard data:', data)

      return data
    } catch (error) {
      console.error('Dashboard API ERROR:', error)
      throw error
    }
  },
  async createTicket(ticket) {
    const response = await fetch(`${apiBaseUrl}/api/tickets`, {
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