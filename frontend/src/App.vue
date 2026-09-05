<script setup>
import { computed, onMounted, ref } from 'vue'
import api from './services/api'

const query = ref('')
const activeFilter = ref('All')
const apiStatus = ref('Checking API...')
const showTicketForm = ref(false)
const activeView = ref('dashboard')

const navItems = [
  { key: 'dashboard', label: 'Dashboard', icon: '▦' },
  { key: 'tickets', label: 'Tickets', icon: '□' },
  { key: 'customers', label: 'Customers', icon: '♙' },
  { key: 'reports', label: 'Reports', icon: '⌁' }
]

const defaultTicket = {
  subject: '',
  customer: '',
  status: 'Open',
  priority: 'High'
}

const newTicket = ref({ ...defaultTicket })
const tickets = ref([])

const openTicketsCount = computed(() => {
  return tickets.value.filter((ticket) => ticket.status === 'Open').length
})

const filteredTickets = computed(() => {
  return tickets.value.filter((ticket) => {
    const matchesSearch =
      ticket.subject.toLowerCase().includes(query.value.toLowerCase()) ||
      ticket.customer.toLowerCase().includes(query.value.toLowerCase())

    const matchesFilter =
      activeFilter.value === 'All' ||
      ticket.status === activeFilter.value

    return matchesSearch && matchesFilter
  })
})

const currentDate = new Date().toLocaleDateString('en-GB', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric'
}).toUpperCase();

const currentTime = new Date().toLocaleTimeString('en-GB', {
  hour: '2-digit',
  minute: '2-digit'
}).toUpperCase();

const loadTickets = async () => {
  try {
    const response = await api.getDashboard()
    tickets.value = response.tickets || []
    apiStatus.value = 'API connected'
  } catch (error) {
    apiStatus.value = 'API offline'
    tickets.value = []
  }
}

const toggleTicketForm = () => {
  showTicketForm.value = !showTicketForm.value
}

const submitTicket = async () => {
  if (!newTicket.value.subject.trim() || !newTicket.value.customer.trim()) {
    return
  }

  try {
    const createdTicket = await api.createTicket({
      subject: newTicket.value.subject.trim(),
      customer: newTicket.value.customer.trim(),
      status: newTicket.value.status,
      priority: newTicket.value.priority
    })

    tickets.value = [createdTicket, ...tickets.value]
    newTicket.value = { ...defaultTicket }
    showTicketForm.value = false
    apiStatus.value = 'API connected'
  } catch (error) {
    apiStatus.value = 'API offline'
  }
}

onMounted(() => {
  loadTickets()
})
</script>
<script>
export default{
  data(){
    return{
      morning: 'Good Morning',
      afternoon: 'Good Afternoon',
      evening: 'Good Evening',
    };
  }
};
</script>
<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">S</div>
        <span>SupportFlow</span>
      </div>

      <div class="workspace-label">WORKSPACE</div>

      <nav class="navigation">
        <button
          v-for="item in navItems"
          :key="item.key"
          type="button"
          class="nav-item"
          :class="{ active: activeView === item.key }"
          @click="activeView = item.key"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          {{ item.label }}
          <span v-if="item.key === 'tickets'" class="nav-count">{{ openTicketsCount }}</span>
        </button>
      </nav>

      <div class="workspace-label settings-label">MANAGE</div>

      <nav class="navigation">
        <a class="nav-item" href="#">
          <span class="nav-icon">⚙</span>
          Settings
        </a>
      </nav>

      <div class="sidebar-bottom">
        <div class="upgrade-card">
          <div class="upgrade-icon">✦</div>
          <strong>Upgrade your plan</strong>
          <p>Unlock advanced reports and automation.</p>
          <button>View plans</button>
        </div>

        <div class="user-card">
          <div class="avatar avatar-purple">EA</div>
          <div>
            <strong>Efatha</strong>
            <span>Administrator</span>
          </div>
          <span class="more-icon">•••</span>
        </div>
      </div>
    </aside>

    <main class="main-content">
      <header class="topbar">
        <div class="mobile-brand">
          <div class="brand-mark">S</div>
          <span>SupportFlow</span>
        </div>

        <div class="topbar-actions">
          <div class="api-status">
            <span
              class="status-dot"
              :class="{ offline: apiStatus === 'API offline' }"
            ></span>
            {{ apiStatus }}
          </div>

          <button class="icon-button">?</button>
          <button class="icon-button notification-button">
            ♢
            <span class="notification-dot"></span>
          </button>
          <div class="avatar avatar-purple">EA</div>
        </div>
      </header>

      <section class="page-content">
        <template v-if="activeView === 'dashboard'">
          <div class="page-heading">
            <div>
              <p class="eyebrow">{{currentDate}}</p>
              <h1 v-if="currentTime <= '12:00' && currentTime > '00:00'"> {{ morning }}</h1>
              <h1 v-else-if="currentTime >= '12:00' && currentTime < '15:10'"> {{ afternoon }}, Efatha</h1>
              <h1 v-else-if="currentTime >= '15:10' && currentTime < '23:59'"> {{ evening }}, Efatha</h1>
              <p class="subtitle">Here is what is happening with your support team today.</p>
            </div>

            <button class="primary-button" @click="toggleTicketForm">
              <span>+</span>
              New ticket
            </button>
          </div>

          <div v-if="showTicketForm" class="panel">
            <div class="panel-heading">
              <div>
                <h2>Create a new ticket</h2>
                <p>Add a support request to the dashboard immediately.</p>
              </div>
            </div>

            <div class="ticket-toolbar">
              <div class="search-box">
                <input v-model="newTicket.subject" type="text" placeholder="Customer issue" />
              </div>

              <div class="search-box">
                <input v-model="newTicket.customer" type="text" placeholder="Jane Doe" />
              </div>

              <select v-model="newTicket.status" class="filter-select">
                <option>Open</option>
                <option>In Progress</option>
                <option>Resolved</option>
              </select>

              <select v-model="newTicket.priority" class="filter-select">
                <option>Urgent</option>
                <option>High</option>
                <option>Medium</option>
                <option>Low</option>
              </select>
            </div>

            <div class="ticket-form-actions">
              <button class="secondary-button" type="button" @click="showTicketForm = false">Cancel</button>
              <button class="primary-button" type="button" @click="submitTicket">Save ticket</button>
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Total tickets</span>
                <span class="stat-icon blue">□</span>
              </div>
              <strong class="stat-value">248</strong>
              <span class="stat-change positive">↑ 12.5% <small>vs last month</small></span>
            </div>

            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Open tickets</span>
                <span class="stat-icon orange">◷</span>
              </div>
              <strong class="stat-value">{{ openTicketsCount }}</strong>
              <span class="stat-change positive">↓ 8.2% <small>vs last month</small></span>
            </div>

            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Avg. response time</span>
                <span class="stat-icon purple">↗</span>
              </div>
              <strong class="stat-value">2h 14m</strong>
              <span class="stat-change positive">↓ 18.4% <small>vs last month</small></span>
            </div>

            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Satisfaction score</span>
                <span class="stat-icon green">♡</span>
              </div>
              <strong class="stat-value">94.8%</strong>
              <span class="stat-change positive">↑ 4.6% <small>vs last month</small></span>
            </div>
          </div>

          <div class="content-grid">
            <section class="panel tickets-panel">
              <div class="panel-heading">
                <div>
                  <h2>Recent tickets</h2>
                  <p>Manage and respond to your latest requests.</p>
                </div>
                <a href="#" class="view-link">View all tickets →</a>
              </div>

              <div class="ticket-toolbar">
                <div class="search-box">
                  <span>⌕</span>
                  <input
                    v-model="query"
                    type="text"
                    placeholder="Search tickets..."
                  />
                </div>

                <select v-model="activeFilter" class="filter-select">
                  <option>All</option>
                  <option>Open</option>
                  <option>In Progress</option>
                  <option>Resolved</option>
                </select>
              </div>

              <div class="ticket-table">
                <div class="table-header">
                  <span>Ticket</span>
                  <span>Customer</span>
                  <span>Status</span>
                  <span>Priority</span>
                  <span>Updated</span>
                </div>

                <div
                  v-for="ticket in filteredTickets"
                  :key="ticket.id"
                  class="table-row"
                >
                  <div class="ticket-title">
                    <strong>{{ ticket.subject }}</strong>
                    <span>{{ ticket.id }}</span>
                  </div>

                  <div class="customer">
                    <div class="avatar avatar-small">{{ ticket.initials }}</div>
                    <span>{{ ticket.customer }}</span>
                  </div>

                  <div>
                    <span
                      class="status-badge"
                      :class="ticket.status.toLowerCase().replace(' ', '-')"
                    >
                      {{ ticket.status === 'Open' ? 'Open ticket' : ticket.status }}
                    </span>
                  </div>

                  <div>
                    <span
                      class="priority"
                      :class="ticket.priority.toLowerCase()"
                    >
                      <span>●</span>
                      {{ ticket.priority }}
                    </span>
                  </div>

                  <span class="updated-time">{{ ticket.time }}</span>
                </div>

                <div v-if="filteredTickets.length === 0" class="empty-state">
                  No tickets match your search.
                </div>
              </div>
            </section>

            <section class="panel activity-panel">
              <div class="panel-heading">
                <div>
                  <h2>Team activity</h2>
                  <p>What your team has been doing.</p>
                </div>
                <button class="more-button">•••</button>
              </div>

              <div class="activity-list">
                <div class="activity-item">
                  <div class="activity-avatar blue-avatar">JM</div>
                  <div>
                    <p><strong>James Miller</strong> resolved ticket <b>#1042</b></p>
                    <span>8 minutes ago</span>
                  </div>
                </div>

                <div class="activity-item">
                  <div class="activity-avatar green-avatar">AK</div>
                  <div>
                    <p><strong>Amelia Kim</strong> added a note to <b>#1039</b></p>
                    <span>24 minutes ago</span>
                  </div>
                </div>

                <div class="activity-item">
                  <div class="activity-avatar orange-avatar">RT</div>
                  <div>
                    <p><strong>Ryan Thomas</strong> assigned ticket <b>#1047</b></p>
                    <span>1 hour ago</span>
                  </div>
                </div>

                <div class="activity-item">
                  <div class="activity-avatar purple-avatar">EA</div>
                  <div>
                    <p><strong>Efatha</strong> created a new team</p>
                    <span>2 hours ago</span>
                  </div>
                </div>
              </div>

              <button class="activity-button">View team activity</button>
            </section>
          </div>
        </template>

        <template v-else-if="activeView === 'tickets'">
          <div class="page-heading">
            <div>
              <p class="eyebrow">WORKSPACE</p>
              <h1>Tickets</h1>
              <p class="subtitle">Review and manage all support requests.</p>
            </div>
          </div>

          <div class="panel">
            <div class="ticket-toolbar">
              <div class="search-box">
                <span>⌕</span>
                <input v-model="query" type="text" placeholder="Search tickets..." />
              </div>

              <select v-model="activeFilter" class="filter-select">
                <option>All</option>
                <option>Open</option>
                <option>In Progress</option>
                <option>Resolved</option>
              </select>
            </div>

            <div class="ticket-table">
              <div class="table-header">
                <span>Ticket</span>
                <span>Customer</span>
                <span>Status</span>
                <span>Priority</span>
                <span>Updated</span>
              </div>

              <div v-for="ticket in filteredTickets" :key="ticket.id" class="table-row">
                <div class="ticket-title">
                  <strong>{{ ticket.subject }}</strong>
                  <span>{{ ticket.id }}</span>
                </div>

                <div class="customer">
                  <div class="avatar avatar-small">{{ ticket.initials }}</div>
                  <span>{{ ticket.customer }}</span>
                </div>

                <div>
                  <span class="status-badge" :class="ticket.status.toLowerCase().replace(' ', '-')">
                    {{ ticket.status === 'Open' ? 'Open ticket' : ticket.status }}
                  </span>
                </div>

                <div>
                  <span class="priority" :class="ticket.priority.toLowerCase()">
                    <span>●</span>
                    {{ ticket.priority }}
                  </span>
                </div>

                <span class="updated-time">{{ ticket.time }}</span>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeView === 'customers'">
          <div class="page-heading">
            <div>
              <p class="eyebrow">WORKSPACE</p>
              <h1>Customers</h1>
              <p class="subtitle">Track the customer accounts behind every request.</p>
            </div>
          </div>

          <div class="panel">
            <div class="activity-list">
              <div class="activity-item">
                <div class="activity-avatar blue-avatar">SJ</div>
                <div>
                  <p><strong>Sarah Johnson</strong> has 2 active tickets</p>
                  <span>Last contact 12 minutes ago</span>
                </div>
              </div>

              <div class="activity-item">
                <div class="activity-avatar green-avatar">DS</div>
                <div>
                  <p><strong>David Smith</strong> has 1 ticket in progress</p>
                  <span>Last contact 45 minutes ago</span>
                </div>
              </div>

              <div class="activity-item">
                <div class="activity-avatar orange-avatar">GW</div>
                <div>
                  <p><strong>Grace Williams</strong> is satisfied and resolved</p>
                  <span>Last contact 2 hours ago</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeView === 'reports'">
          <div class="page-heading">
            <div>
              <p class="eyebrow">WORKSPACE</p>
              <h1>Reports</h1>
              <p class="subtitle">Performance metrics across the support team.</p>
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Resolution rate</span>
                <span class="stat-icon blue">□</span>
              </div>
              <strong class="stat-value">87%</strong>
              <span class="stat-change positive">↑ 5.1% <small>vs last month</small></span>
            </div>

            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Escalations</span>
                <span class="stat-icon orange">◷</span>
              </div>
              <strong class="stat-value">12</strong>
              <span class="stat-change positive">↓ 2.4% <small>vs last month</small></span>
            </div>

            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Response SLA</span>
                <span class="stat-icon purple">↗</span>
              </div>
              <strong class="stat-value">96%</strong>
              <span class="stat-change positive">↑ 3.7% <small>vs last month</small></span>
            </div>
          </div>
        </template>
      </section>
    </main>
  </div>
</template>

