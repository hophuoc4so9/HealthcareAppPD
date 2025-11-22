package com.example.healthcareapppd.utils

import com.example.healthcareapppd.data.api.model.DoctorProfile

/**
 * Utility class for filtering and sorting doctor lists
 */
object DoctorFilterHelper {
    
    /**
     * Filter doctors by specialization
     */
    fun filterBySpecialization(
        doctors: List<DoctorProfile>,
        specialization: String
    ): List<DoctorProfile> {
        if (specialization.isBlank()) return doctors
        return doctors.filter { 
            it.specialization.contains(specialization, ignoreCase = true)
        }
    }
    
    /**
     * Filter doctors by verification status
     */
    fun filterByStatus(
        doctors: List<DoctorProfile>,
        status: String
    ): List<DoctorProfile> {
        return doctors.filter { it.verificationStatus == status || it.status == status }
    }
    
    /**
     * Sort doctors by years of experience (descending)
     */
    fun sortByExperience(doctors: List<DoctorProfile>): List<DoctorProfile> {
        return doctors.sortedByDescending { it.yearsOfExperience ?: 0 }
    }
    
    /**
     * Sort doctors by name (alphabetically)
     */
    fun sortByName(doctors: List<DoctorProfile>): List<DoctorProfile> {
        return doctors.sortedBy { it.fullName }
    }
    
    /**
     * Search doctors by name or specialization
     */
    fun searchDoctors(
        doctors: List<DoctorProfile>,
        query: String
    ): List<DoctorProfile> {
        if (query.isBlank()) return doctors
        
        return doctors.filter { doctor ->
            doctor.fullName.contains(query, ignoreCase = true) ||
            doctor.specialization.contains(query, ignoreCase = true) ||
            doctor.hospitalAffiliation?.contains(query, ignoreCase = true) == true
        }
    }
    
    /**
     * Get unique list of specializations
     */
    fun getSpecializations(doctors: List<DoctorProfile>): List<String> {
        return doctors.map { it.specialization }.distinct().sorted()
    }
    
    /**
     * Group doctors by specialization
     */
    fun groupBySpecialization(doctors: List<DoctorProfile>): Map<String, List<DoctorProfile>> {
        return doctors.groupBy { it.specialization }
    }
    
    /**
     * Get doctors count by status
     */
    fun countByStatus(doctors: List<DoctorProfile>): Map<String, Int> {
        return doctors.mapNotNull { it.verificationStatus ?: it.status }
            .groupBy { it }
            .mapValues { it.value.size }
    }
    
    /**
     * Get top experienced doctors
     */
    fun getTopExperienced(
        doctors: List<DoctorProfile>,
        limit: Int = 10
    ): List<DoctorProfile> {
        return doctors.sortedByDescending { it.yearsOfExperience ?: 0 }
            .take(limit)
    }
}
