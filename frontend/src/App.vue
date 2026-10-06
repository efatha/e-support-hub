<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import api from './services/api'

const morning = 'Good Morning'
const afternoon = 'Good Afternoon'
const evening = 'Good Evening'

const query = ref('')
const activeFilter = ref('All')
const apiStatus = ref('Checking API...')
const showTicketForm = ref(false)
const activeView = ref('dashboard')

// Reactive timer to force recalculation of relative times every 60 seconds
const nowTimer = ref(Date.now())
let timerInterval = null

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
const ticketErrors = ref({})

const settingsStorageKey = 'e_support_settings'

const defaultSettings = {
  workspaceName: 'SupportFlow',
  supportEmail: '',
  displayName: 'Efatha',
  timezone: 'Africa/Nairobi',
  defaultStatus: 'Open',
  defaultPriority: 'High',
  notifyOnNewTicket: true
}

const timezoneOptions = [
  'Africa/Nairobi',
  'Africa/Lagos',
  'Europe/London',
  'Europe/Paris',
  'Asia/Dubai',
  'Asia/Kolkata',
  'America/New_York',
  'America/Los_Angeles',
  'UTC'
]

const settings = ref({ ...defaultSettings })
const settingsForm = ref({ ...defaultSettings })
const settingsErrors = ref({})
const settingsMessage = ref('')

const userInitials = computed(() => {
  const parts = settings.value.displayName.trim().split(/\s+/).filter(Boolean)

  if (parts.length === 0) {
    return 'EA'
  }

  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase()
  }

  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
})
const tickets = ref([])
const notificationCount = ref(0)
const notifications = ref([])
const showNotificationPanel = ref(false)

const notificationStorageKey = 'e_support_notifications'
const notificationRetentionMs = 7 * 24 * 60 * 60 * 1000

// Helper functions for Local Storage persistence
const getLocalTickets = () => {
  try {
    const saved = localStorage.getItem('e_support_tickets')
    return saved ? JSON.parse(saved) : []
  } catch (e) {
    return []
  }
}

const saveLocalTickets = (ticketList) => {
  try {
    localStorage.setItem('e_support_tickets', JSON.stringify(ticketList))
  } catch (e) {
    console.error('Failed to save to localStorage', e)
  }
}

const getLocalNotifications = () => {
  try {
    const saved = JSON.parse(localStorage.getItem(notificationStorageKey) || '[]')
    const cutoff = Date.now() - notificationRetentionMs
    const recent = saved.filter((notification) => {
      const createdAt = new Date(notification.createdAt).getTime()
      return !Number.isNaN(createdAt) && createdAt >= cutoff
    })

    localStorage.setItem(notificationStorageKey, JSON.stringify(recent))
    return recent
  } catch (error) {
    return []
  }
}

const saveLocalNotifications = (notificationList) => {
  try {
    localStorage.setItem(notificationStorageKey, JSON.stringify(notificationList))
  } catch (error) {
    console.error('Failed to save notifications to localStorage', error)
  }
}

const mergeNotifications = (serverNotifications) => {
  const notificationByTicket = new Map()

  for (const notification of [...getLocalNotifications(), ...serverNotifications]) {
    const key = notification.ticketId || notification.id
    notificationByTicket.set(key, {
      ...notification,
      read: notification.read === true
    })
  }

  const merged = [...notificationByTicket.values()]
    .sort((first, second) => new Date(second.createdAt) - new Date(first.createdAt))

  notifications.value = merged
  saveLocalNotifications(merged)
  notificationCount.value = merged.filter((notification) => !notification.read).length
}

const openTicketsCount = computed(() => {
  return tickets.value.filter((ticket) => ticket.status === 'Open').length
})

const totalTicketsCount = computed(() => {
  return tickets.value.length
})

const filteredTickets = computed(() => {
  return tickets.value.filter((ticket) => {
    const matchesSearch =
      ticket.subject.toLowerCase().includes(query.value.toLowerCase()) ||
      ticket.customer.toLowerCase().includes(query.value.toLowerCase())

    const matchesFilter =
      activeFilter.value === 'All' || ticket.status === activeFilter.value

    return matchesSearch && matchesFilter
  })
})

const ticketsPerPage = 5
const recentTicketsPage = ref(1)

const recentTicketsPageCount = computed(() => {
  return Math.max(1, Math.ceil(filteredTickets.value.length / ticketsPerPage))
})

const recentTickets = computed(() => {
  const page = Math.min(recentTicketsPage.value, recentTicketsPageCount.value)
  const start = (page - 1) * ticketsPerPage
  return filteredTickets.value.slice(start, start + ticketsPerPage)
})

const recentTicketPages = computed(() => {
  const total = recentTicketsPageCount.value
  const current = Math.min(recentTicketsPage.value, total)

  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => index + 1)
  }

  const visible = new Set([1, total, current, current - 1, current + 1])
  return [...visible]
    .filter((page) => page >= 1 && page <= total)
    .sort((first, second) => first - second)
})

watch([query, activeFilter], () => {
  recentTicketsPage.value = 1
})

watch(recentTicketsPageCount, (pageCount) => {
  if (recentTicketsPage.value > pageCount) {
    recentTicketsPage.value = pageCount
  }
})

const currentDate = new Date().toLocaleDateString('en-GB', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric'
}).toUpperCase()

const currentTime = new Date().toLocaleTimeString('en-GB', {
  hour: '2-digit',
  minute: '2-digit'
}).toUpperCase()

const parseTicketDate = (rawDate) => {
  if (!rawDate) {
    return null
  }
  // Convert:
  // 2026-09-10 08:32:27.671618
  // into:
  // 2026-09-10T08:32:27.671618
  const normalizedDate = rawDate.replace(' ', 'T')

  const timestamp = new Date(normalizedDate).getTime()

  return Number.isNaN(timestamp) ? null : timestamp
}

const formatTimeAgo = (rawDate) => {
  const createdAt = parseTicketDate(rawDate)

  if (createdAt === null) {
    return 'Just now'
  }

  const differenceInSeconds = Math.floor(
    (nowTimer.value - createdAt) / 1000
  )

  if (differenceInSeconds < 60) {
    return 'Just now'
  }
  const minutes = Math.floor(differenceInSeconds / 60)
  if (minutes < 60) {
    return `${minutes} min ago`
  }

  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours} hour${hours === 1 ? '' : 's'} ago`
  }

  const days = Math.floor(hours / 24)
  if (days < 7) {
    return `${days} day${days === 1 ? '' : 's'} ago`
  }

  const weeks = Math.floor(days / 7)
  if (weeks < 4) {
    return `${weeks} week${weeks === 1 ? '' : 's'} ago`
  }

  const months = Math.floor(days / 30)
  if (months < 12) {
    return `${months} month${months === 1 ? '' : 's'} ago`
  }

  const years = Math.floor(days / 365)
  return `${years} year${years === 1 ? '' : 's'} ago`
}

const loadTickets = async () => {
  try {
    const response = await api.getDashboard()

    tickets.value = response.tickets || []

    // LocalStorage is only a cache of the backend data.
    saveLocalTickets(tickets.value)

    apiStatus.value = 'API connected'
  } catch (error) {
    console.error('Failed to load tickets:', error)

    apiStatus.value = 'API unavailable'
  }
}

const freshTicket = () => ({
  subject: '',
  customer: '',
  status: settings.value.defaultStatus || defaultTicket.status,
  priority: settings.value.defaultPriority || defaultTicket.priority
})

const toggleTicketForm = () => {
  if (!showTicketForm.value && !newTicket.value.subject && !newTicket.value.customer) {
    newTicket.value = freshTicket()
  }

  ticketErrors.value = {}
  showTicketForm.value = !showTicketForm.value
}

const submitTicket = async () => {
  const subject = newTicket.value.subject.trim()
  const customer = newTicket.value.customer.trim()
  const errors = {}

  if (!subject) {
    errors.subject = 'Subject is required.'
  }

  if (!customer) {
    errors.customer = 'Customer name is required.'
  }

  ticketErrors.value = errors

  if (subject === '' || customer === '') {
    return
  }

  try {
    const createdTicket = await api.createTicket({
      subject,
      customer,
      status: newTicket.value.status,
      priority: newTicket.value.priority
    })

    const updatedList = [
      createdTicket,
      ...tickets.value
    ]

    tickets.value = updatedList

    // Save the backend response as the local cache.
    saveLocalTickets(updatedList)

    newTicket.value = freshTicket()
    ticketErrors.value = {}

    showTicketForm.value = false

    apiStatus.value = 'API connected'
    await loadNotifications()
  } catch (error) {
    console.error('Failed to create ticket:', error)

    apiStatus.value = 'API unavailable'
  }
}

const loadSettings = () => {
  try {
    const saved = JSON.parse(localStorage.getItem(settingsStorageKey) || 'null')

    if (saved && typeof saved === 'object') {
      settings.value = { ...defaultSettings, ...saved }
    }
  } catch (error) {
    settings.value = { ...defaultSettings }
  }

  settingsForm.value = { ...settings.value }
}

const isValidEmail = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)

const openSettings = () => {
  if (activeView.value !== 'settings') {
    settingsForm.value = { ...settings.value }
    settingsErrors.value = {}
    settingsMessage.value = ''
  }

  activeView.value = 'settings'
}

const clearSettingsFeedback = (field) => {
  settingsMessage.value = ''

  if (!settingsErrors.value[field]) {
    return
  }

  const nextErrors = { ...settingsErrors.value }
  delete nextErrors[field]
  settingsErrors.value = nextErrors
}

const saveSettings = () => {
  const next = {
    workspaceName: settingsForm.value.workspaceName.trim(),
    supportEmail: settingsForm.value.supportEmail.trim(),
    displayName: settingsForm.value.displayName.trim(),
    timezone: settingsForm.value.timezone,
    defaultStatus: settingsForm.value.defaultStatus,
    defaultPriority: settingsForm.value.defaultPriority,
    notifyOnNewTicket: Boolean(settingsForm.value.notifyOnNewTicket)
  }
  const errors = {}

  if (!next.workspaceName) {
    errors.workspaceName = 'Workspace name is required.'
  }

  if (!next.supportEmail) {
    errors.supportEmail = 'Support email is required.'
  } else if (!isValidEmail(next.supportEmail)) {
    errors.supportEmail = 'Enter a valid email address.'
  }

  if (!next.displayName) {
    errors.displayName = 'Display name is required.'
  }

  if (!next.timezone) {
    errors.timezone = 'Timezone is required.'
  }

  if (!next.defaultStatus) {
    errors.defaultStatus = 'Default status is required.'
  }

  if (!next.defaultPriority) {
    errors.defaultPriority = 'Default priority is required.'
  }

  settingsErrors.value = errors
  settingsMessage.value = ''

  if (Object.keys(errors).length > 0) {
    return
  }

  settings.value = next
  settingsForm.value = { ...next }
  localStorage.setItem(settingsStorageKey, JSON.stringify(next))
  settingsMessage.value = 'Settings saved.'
}

onMounted(() => {
  loadSettings()
  loadTickets()
  loadNotifications()
  window.addEventListener('click', closeNotificationPanel)
  // Ticker updates reactive timer every minute to continuously recalculate "X min ago"
  timerInterval = setInterval(() => {
    nowTimer.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  window.removeEventListener('click', closeNotificationPanel)
})

const loadNotifications = async () => {
  try {
    const data = await api.getNotifications()

    mergeNotifications(data.notifications || [])
  } catch (error) {
    console.error('Failed to load notifications:', error)
    mergeNotifications([])
  }
}

const toggleNotificationPanel = async () => {
  showNotificationPanel.value = !showNotificationPanel.value

  if (showNotificationPanel.value) {
    await loadNotifications()

    if (notificationCount.value > 0) {
      await api.markNotificationsRead()
      const readNotifications = notifications.value.map((notification) => ({
        ...notification,
        read: true
      }))
      notifications.value = readNotifications
      saveLocalNotifications(readNotifications)
      notificationCount.value = 0
    }
  }
}

const closeNotificationPanel = () => {
  showNotificationPanel.value = false
}
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
        <button
          type="button"
          class="nav-item"
          :class="{ active: activeView === 'settings' }"
          @click="openSettings"
        >
          <span class="nav-icon">⚙</span>
          Settings
        </button>
      </nav>

      <div class="sidebar-bottom">
        <div class="upgrade-card">
          <div class="upgrade-icon">✦</div>
          <strong>Upgrade your plan</strong>
          <p>Unlock advanced reports and automation.</p>
          <button>View plans</button>
        </div>

        <div class="user-card">
          <div class="avatar avatar-purple">{{ userInitials }}</div>
          <div>
            <strong>{{ settings.displayName }}</strong>
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
          <div class="notification-wrapper" @click.stop>
            <button class="icon-button notification-button" @click="toggleNotificationPanel">
              🔔
              <span v-if="notificationCount > 0" class="notification-count">{{ notificationCount }}</span>
            </button>

            <div v-if="showNotificationPanel" class="notification-panel" @click.stop>
              <div class="notification-panel-heading">Notifications</div>

              <div v-if="notifications.length === 0" class="notification-empty">
                No notifications yet.
              </div>

              <div
                v-for="notification in notifications"
                :key="notification.id"
                class="notification-card"
              >
                {{ notification.message }}
              </div>
            </div>
          </div>
          <div class="avatar avatar-purple">{{ userInitials }}</div>
        </div>
      </header>

      <section class="page-content">
        <template v-if="activeView === 'dashboard'">
          <div class="page-heading">
            <div>
              <p class="eyebrow">{{ currentDate }}</p>
              <h1 v-if="currentTime <= '12:00' && currentTime > '00:00'">{{ morning }}, {{ settings.displayName }}</h1>
              <h1 v-else-if="currentTime >= '12:00' && currentTime < '16:00'">{{ afternoon }}, {{ settings.displayName }}</h1>
              <h1 v-else-if="currentTime >= '16:00' && currentTime <= '23:59'">{{ evening }}, {{ settings.displayName }}</h1>
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

            <form class="settings-grid" novalidate @submit.prevent="submitTicket">
              <label class="settings-field">
                <span>Subject <span class="required-mark" aria-hidden="true">*</span></span>
                <input
                  v-model="newTicket.subject"
                  type="text"
                  placeholder="Customer issue"
                  aria-required="true"
                  :aria-invalid="Boolean(ticketErrors.subject)"
                  :class="{ invalid: ticketErrors.subject }"
                  @input="ticketErrors.subject = ''"
                />
                <small v-if="ticketErrors.subject" class="field-error">{{ ticketErrors.subject }}</small>
              </label>

              <label class="settings-field">
                <span>Customer <span class="required-mark" aria-hidden="true">*</span></span>
                <input
                  v-model="newTicket.customer"
                  type="text"
                  placeholder="Jane Doe"
                  aria-required="true"
                  :aria-invalid="Boolean(ticketErrors.customer)"
                  :class="{ invalid: ticketErrors.customer }"
                  @input="ticketErrors.customer = ''"
                />
                <small v-if="ticketErrors.customer" class="field-error">{{ ticketErrors.customer }}</small>
              </label>

              <label class="settings-field">
                <span>Status</span>
                <select v-model="newTicket.status">
                  <option>Open</option>
                  <option>In Progress</option>
                  <option>Resolved</option>
                </select>
              </label>

              <label class="settings-field">
                <span>Priority</span>
                <select v-model="newTicket.priority">
                  <option>Urgent</option>
                  <option>High</option>
                  <option>Medium</option>
                  <option>Low</option>
                </select>
              </label>

              <div class="span-2 ticket-form-actions">
                <button class="secondary-button" type="button" @click="showTicketForm = false">Cancel</button>
                <button class="primary-button" type="submit">Save ticket</button>
              </div>
            </form>
          </div>

          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-top">
                <span class="stat-label">Total tickets</span>
                <span class="stat-icon blue">□</span>
              </div>
              <strong class="stat-value">{{ totalTicketsCount }}</strong>
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
                  v-for="ticket in recentTickets"
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

                  <span class="updated-time">{{ formatTimeAgo(ticket.createdAt) }}</span>
                </div>

                <div v-if="filteredTickets.length === 0" class="empty-state">
                  No tickets match your search.
                </div>
              </div>

              <nav
                v-if="recentTicketsPageCount > 1"
                class="ticket-pagination"
                aria-label="Recent tickets pages"
              >
                <template v-for="(page, index) in recentTicketPages" :key="page">
                  <span
                    v-if="index > 0 && page - recentTicketPages[index - 1] > 1"
                    class="page-ellipsis"
                  >…</span>
                  <button
                    type="button"
                    class="page-button"
                    :class="{ active: page === recentTicketsPage }"
                    :aria-current="page === recentTicketsPage ? 'page' : undefined"
                    @click="recentTicketsPage = page"
                  >({{ page }})</button>
                </template>
              </nav>
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
                    <p><strong>{{ settings.displayName }}</strong> created a new team</p>
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

                <span class="updated-time">{{ formatTimeAgo(ticket.createdAt) }}</span>
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

        <template v-else-if="activeView === 'settings'">
          <div class="page-heading">
            <div>
              <p class="eyebrow">MANAGE</p>
              <h1>Settings</h1>
              <p class="subtitle">Workspace details used for tickets, greetings, and notifications.</p>
            </div>
          </div>

          <form class="settings-layout" novalidate @submit.prevent="saveSettings">
            <section class="panel">
              <div class="panel-heading">
                <div>
                  <h2>Workspace</h2>
                  <p>Required details for this support workspace.</p>
                </div>
              </div>

              <div class="settings-grid">
                <label class="settings-field">
                  <span>Workspace name <span class="required-mark" aria-hidden="true">*</span></span>
                  <input
                    v-model="settingsForm.workspaceName"
                    type="text"
                    placeholder="SupportFlow"
                    autocomplete="organization"
                    aria-required="true"
                    :aria-invalid="Boolean(settingsErrors.workspaceName)"
                    :class="{ invalid: settingsErrors.workspaceName }"
                    @input="clearSettingsFeedback('workspaceName')"
                  />
                  <small v-if="settingsErrors.workspaceName" class="field-error">{{ settingsErrors.workspaceName }}</small>
                </label>

                <label class="settings-field">
                  <span>Support email <span class="required-mark" aria-hidden="true">*</span></span>
                  <input
                    v-model="settingsForm.supportEmail"
                    type="email"
                    placeholder="support@company.com"
                    autocomplete="email"
                    aria-required="true"
                    :aria-invalid="Boolean(settingsErrors.supportEmail)"
                    :class="{ invalid: settingsErrors.supportEmail }"
                    @input="clearSettingsFeedback('supportEmail')"
                  />
                  <small v-if="settingsErrors.supportEmail" class="field-error">{{ settingsErrors.supportEmail }}</small>
                  <small v-else class="field-hint">Used when new-ticket notifications are on.</small>
                </label>

                <label class="settings-field">
                  <span>Display name <span class="required-mark" aria-hidden="true">*</span></span>
                  <input
                    v-model="settingsForm.displayName"
                    type="text"
                    placeholder="Efatha"
                    autocomplete="name"
                    aria-required="true"
                    :aria-invalid="Boolean(settingsErrors.displayName)"
                    :class="{ invalid: settingsErrors.displayName }"
                    @input="clearSettingsFeedback('displayName')"
                  />
                  <small v-if="settingsErrors.displayName" class="field-error">{{ settingsErrors.displayName }}</small>
                </label>

                <label class="settings-field">
                  <span>Timezone <span class="required-mark" aria-hidden="true">*</span></span>
                  <select
                    v-model="settingsForm.timezone"
                    aria-required="true"
                    :aria-invalid="Boolean(settingsErrors.timezone)"
                    :class="{ invalid: settingsErrors.timezone }"
                    @change="clearSettingsFeedback('timezone')"
                  >
                    <option v-for="zone in timezoneOptions" :key="zone" :value="zone">{{ zone }}</option>
                  </select>
                  <small v-if="settingsErrors.timezone" class="field-error">{{ settingsErrors.timezone }}</small>
                </label>
              </div>
            </section>

            <section class="panel">
              <div class="panel-heading">
                <div>
                  <h2>Ticket defaults</h2>
                  <p>Applied the next time you create a ticket.</p>
                </div>
              </div>

              <div class="settings-grid">
                <label class="settings-field">
                  <span>Default status <span class="required-mark" aria-hidden="true">*</span></span>
                  <select
                    v-model="settingsForm.defaultStatus"
                    aria-required="true"
                    :aria-invalid="Boolean(settingsErrors.defaultStatus)"
                    :class="{ invalid: settingsErrors.defaultStatus }"
                    @change="clearSettingsFeedback('defaultStatus')"
                  >
                    <option>Open</option>
                    <option>In Progress</option>
                    <option>Resolved</option>
                  </select>
                  <small v-if="settingsErrors.defaultStatus" class="field-error">{{ settingsErrors.defaultStatus }}</small>
                </label>

                <label class="settings-field">
                  <span>Default priority <span class="required-mark" aria-hidden="true">*</span></span>
                  <select
                    v-model="settingsForm.defaultPriority"
                    aria-required="true"
                    :aria-invalid="Boolean(settingsErrors.defaultPriority)"
                    :class="{ invalid: settingsErrors.defaultPriority }"
                    @change="clearSettingsFeedback('defaultPriority')"
                  >
                    <option>Urgent</option>
                    <option>High</option>
                    <option>Medium</option>
                    <option>Low</option>
                  </select>
                  <small v-if="settingsErrors.defaultPriority" class="field-error">{{ settingsErrors.defaultPriority }}</small>
                </label>

                <label class="settings-check span-2">
                  <input v-model="settingsForm.notifyOnNewTicket" type="checkbox" />
                  <span>
                    <strong>Email me when a ticket is created</strong>
                    <small>Sends a notice to the support email.</small>
                  </span>
                </label>
              </div>
            </section>

            <div class="settings-actions">
              <p class="settings-required-note">Fields marked with * are required.</p>
              <span v-if="settingsMessage" class="settings-saved" role="status">{{ settingsMessage }}</span>
              <button class="primary-button" type="submit">Save settings</button>
            </div>
          </form>
        </template>
      </section>
    </main>
  </div>
</template>