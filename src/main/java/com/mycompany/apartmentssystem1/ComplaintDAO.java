package com.mycompany.apartmentssystem1;

// =============================================
// 🔴 FOR GROUPMATE (COMPLAINTS)
// =============================================
// Suggested methods:
//
// public void fileComplaint(int tenantId, int apartmentId, String message) {
//     // tenant submits complaint
//     // frontend: tenant dashboard → "File Complaint"
// }
//
// public List<String> getComplaintsByTenant(int tenantId) {
//     // tenant sees own complaints with status
// }
//
// public List<String> getAllComplaints() {
//     // owner sees all complaints
//     // frontend: owner dashboard → "View Complaints"
// }
//
// public void updateComplaintStatus(int complaintId, String status) {
//     // owner changes status: Pending → Working → Done
//     // when Done, automatically move to history
// }
//
// public List<String> getComplaintHistory(int tenantId) {
//     // tenant sees old, resolved complaints
// }
//
// Table suggestion:
// CREATE TABLE complaints (
//   complaint_id INTEGER PRIMARY KEY,
//   tenant_id INTEGER,
//   apartment_id INTEGER,
//   message TEXT,
//   status TEXT,
//   date_filed TEXT,
//   date_completed TEXT
// );
// =============================================

public class ComplaintDAO {
    // TODO: implement your methods here
}