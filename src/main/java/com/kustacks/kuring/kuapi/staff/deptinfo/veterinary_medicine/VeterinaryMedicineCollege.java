package com.kustacks.kuring.kuapi.staff.deptinfo.veterinary_medicine;

import com.kustacks.kuring.kuapi.staff.deptinfo.DeptInfo;

// 수의예과 2년 -> 수의학과 4년 프로세스이므로 수의학과로 통일
public class VeterinaryMedicineCollege extends DeptInfo {
    
    public VeterinaryMedicineCollege(String code, String deptName, String... pfForumIds) {
        super(code, deptName, "수의과대학", pfForumIds);
    }
}
