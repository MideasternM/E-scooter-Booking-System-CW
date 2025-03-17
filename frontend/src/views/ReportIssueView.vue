<template>
  <div class="report-issue-container">
    <div class="header">
      <router-link :to="`/booking/${bookingId}`" class="back-button">
        <span>&larr;</span> Back to Booking Details
      </router-link>
      <h1>Report Issue</h1>
      <p class="subtitle">Booking #{{ bookingId }}</p>
    </div>

    <div class="report-form">
      <form @submit.prevent="submitReport">
        <div class="form-group">
          <label for="issueType">Issue Type</label>
          <select 
            id="issueType" 
            v-model="formData.issueType" 
            required
          >
            <option value="">Select an issue type</option>
            <option value="mechanical">Mechanical Problem</option>
            <option value="electrical">Electrical Problem</option>
            <option value="cosmetic">Cosmetic Damage</option>
            <option value="safety">Safety Concern</option>
            <option value="other">Other</option>
          </select>
        </div>

        <div class="form-group">
          <label for="severity">Severity Level</label>
          <div class="severity-options">
            <label class="severity-option">
              <input 
                type="radio" 
                v-model="formData.severity" 
                value="low"
                required
              >
              <span class="severity-label low">Low</span>
            </label>
            <label class="severity-option">
              <input 
                type="radio" 
                v-model="formData.severity" 
                value="medium"
              >
              <span class="severity-label medium">Medium</span>
            </label>
            <label class="severity-option">
              <input 
                type="radio" 
                v-model="formData.severity" 
                value="high"
              >
              <span class="severity-label high">High</span>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea
            id="description"
            v-model="formData.description"
            rows="5"
            placeholder="Please provide detailed information about the issue..."
            required
          ></textarea>
          <div class="character-count" :class="{ 'near-limit': characterCount > 450 }">
            {{ characterCount }}/500 characters
          </div>
        </div>

        <div class="form-group">
          <label for="location">Location When Issue Occurred</label>
          <input
            type="text"
            id="location"
            v-model="formData.location"
            placeholder="Enter the location"
            required
          >
        </div>

        <div class="form-group">
          <label class="photo-upload">
            <input
              type="file"
              accept="image/*"
              multiple
              @change="handleFileUpload"
              class="hidden"
            >
            <div class="upload-button">
              <span>Upload Photos</span>
              <small>(Optional, max 3 photos)</small>
            </div>
          </label>
          <div class="photo-preview" v-if="formData.photos.length">
            <div v-for="(photo, index) in formData.photos" :key="index" class="photo-item">
              <img :src="photo.preview" alt="Issue photo preview">
              <button type="button" class="remove-photo" @click="removePhoto(index)">&times;</button>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button 
            type="button" 
            class="cancel-button"
            @click="cancelReport"
          >
            Cancel
          </button>
          <button 
            type="submit" 
            class="submit-button"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Submitting...' : 'Submit Report' }}
          </button>
        </div>
      </form>
    </div>

    <div v-if="showSuccessModal" class="modal">
      <div class="modal-content">
        <h2>Report Submitted Successfully</h2>
        <p>Your issue report has been received. Reference number: #{{ reportId }}</p>
        <button @click="goToBookingDetails" class="modal-button">Back to Booking Details</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const bookingId = route.params.id
const isSubmitting = ref(false)
const showSuccessModal = ref(false)
const reportId = ref('')

interface PhotoData {
  file: File
  preview: string
}

interface FormData {
  issueType: string
  severity: string
  description: string
  location: string
  photos: PhotoData[]
}

const formData = ref<FormData>({
  issueType: '',
  severity: '',
  description: '',
  location: '',
  photos: []
})

const characterCount = computed(() => formData.value.description.length)

const handleFileUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files) return

  const newFiles = Array.from(input.files)
  const maxPhotos = 3 - formData.value.photos.length
  
  newFiles.slice(0, maxPhotos).forEach(file => {
    const reader = new FileReader()
    reader.onload = (e) => {
      formData.value.photos.push({
        file,
        preview: e.target?.result as string
      })
    }
    reader.readAsDataURL(file)
  })
}

const removePhoto = (index: number) => {
  formData.value.photos.splice(index, 1)
}

const submitReport = async () => {
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Generate a random report ID
    reportId.value = Math.random().toString(36).substr(2, 9).toUpperCase()
    
    showSuccessModal.value = true
  } catch (error) {
    console.error('Error submitting report:', error)
    alert('Failed to submit report. Please try again.')
  } finally {
    isSubmitting.value = false
  }
}

const cancelReport = () => {
  if (confirm('Are you sure you want to cancel? All entered information will be lost.')) {
    router.back()
  }
}

const goToBookingDetails = () => {
  router.push(`/booking/${bookingId}`)
}
</script>

<style scoped>
.report-issue-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  margin-bottom: 2rem;
}

.back-button {
  display: inline-flex;
  align-items: center;
  color: #606f7b;
  text-decoration: none;
  margin-bottom: 1rem;
}

.back-button:hover {
  color: #2c3e50;
}

.back-button span {
  margin-right: 0.5rem;
}

h1 {
  color: #2c3e50;
  margin: 0 0 0.5rem;
}

.subtitle {
  color: #606f7b;
  margin: 0;
}

.report-form {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

select,
input[type="text"],
textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

select:focus,
input[type="text"]:focus,
textarea:focus {
  outline: none;
  border-color: #42b983;
  box-shadow: 0 0 0 2px rgba(66, 185, 131, 0.1);
}

.severity-options {
  display: flex;
  gap: 1rem;
}

.severity-option {
  flex: 1;
  cursor: pointer;
}

.severity-option input {
  display: none;
}

.severity-label {
  display: block;
  padding: 0.75rem;
  text-align: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: all 0.3s;
}

.severity-option input:checked + .severity-label {
  color: white;
  border-color: transparent;
}

.severity-label.low {
  color: #42b983;
}

.severity-label.medium {
  color: #f39c12;
}

.severity-label.high {
  color: #e74c3c;
}

.severity-option input:checked + .severity-label.low {
  background-color: #42b983;
}

.severity-option input:checked + .severity-label.medium {
  background-color: #f39c12;
}

.severity-option input:checked + .severity-label.high {
  background-color: #e74c3c;
}

.character-count {
  text-align: right;
  color: #606f7b;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.character-count.near-limit {
  color: #e74c3c;
}

.photo-upload {
  cursor: pointer;
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem;
  border: 2px dashed #ddd;
  border-radius: 4px;
  transition: all 0.3s;
}

.upload-button:hover {
  border-color: #42b983;
}

.upload-button small {
  color: #606f7b;
  margin-top: 0.5rem;
}

.hidden {
  display: none;
}

.photo-preview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.photo-item {
  position: relative;
}

.photo-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.remove-photo {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e74c3c;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.cancel-button,
.submit-button {
  flex: 1;
  padding: 0.875rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.cancel-button {
  background-color: #606f7b;
  color: white;
}

.submit-button {
  background-color: #42b983;
  color: white;
}

.cancel-button:hover,
.submit-button:hover {
  opacity: 0.9;
}

.submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
  max-width: 400px;
  width: 90%;
}

.modal-content h2 {
  color: #42b983;
  margin: 0 0 1rem;
}

.modal-button {
  margin-top: 1.5rem;
  padding: 0.75rem 1.5rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: opacity 0.3s;
}

.modal-button:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .severity-options {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-button,
  .submit-button {
    width: 100%;
  }
}
</style> 