<script setup>
import { onMounted, ref } from 'vue'

const message = ref('Loading...')
const error = ref('')

onMounted(async () => {
  try {
    const response = await fetch('/api/hello')
    if (!response.ok) {
      throw new Error(`Request failed: ${response.status}`)
    }
    const data = await response.json()
    message.value = data.message
  } catch (err) {
    error.value = 'Could not connect to Spring Boot'
    console.error(err)
  }
})
</script>

<template>
  <main>
    <h1>My Full Stack App</h1>
    <p v-if="error">{{ error }}</p>
    <p v-else>{{ message }}</p>
  </main>
</template>