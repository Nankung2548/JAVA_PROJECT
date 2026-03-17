package com.lab.Project_JAVA;

import java.util.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class RunQuery {

    @SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
    public static void main(String[] args) {

        SessionFactory sf = HibernateConnection.doHibernateConnection();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();

        try {
            // 1. งานที่ซ่อมเสร็จไว (ไม่เกิน 1 สัปดาห์)
            System.out.println("\n===== 1. รายการงานซ่อมด่วนที่เสร็จภายใน 7 วัน =====");
            String hql1 = "SELECT r.jobID, r.modelName, function('DATEDIFF', r.appoinmentDate, r.dateReceived) " +
                          "FROM RepairJob r WHERE function('DATEDIFF', r.appoinmentDate, r.dateReceived) < 7";
            List<Object[]> list1 = session.createQuery(hql1).list();
            for (Object[] row : list1) {
                System.out.println("รหัสงาน: " + row[0] + " | รุ่น: " + row[1] + " | ใช้เวลาจริง: " + row[2] + " วัน");
            }

            // 2. สรุปภาพรวมงานในระบบ
            System.out.println("\n===== 2. สรุปจำนวนเครื่องซ่อมแยกตามประเภทอุปกรณ์ =====");
            String hql2 = "SELECT r.typeName, COUNT(r.jobID) FROM RepairJob r GROUP BY r.typeName";
            List<Object[]> list2 = session.createQuery(hql2).list();
            for (Object[] row : list2) {
                System.out.println("ประเภทอุปกรณ์: " + row[0] + " | จำนวน: " + row[1] + " เครื่อง");
            }

            // 3. ข้อมูลการมอบหมายงาน
            System.out.println("\n===== 3. รายชื่อช่างที่รับผิดชอบดูแลงานซ่อมปัจจุบัน =====");
            String hql3 = "SELECT r.jobID, t.TechName FROM RepairJob r JOIN r.technician t";
            List<Object[]> list3 = session.createQuery(hql3).list();
            for (Object[] row : list3) {
                System.out.println("งานเลขที่: " + row[0] + " | ช่างผู้ดูแล: " + row[1]);
            }

            // 4. ตรวจสอบประวัติการใช้บริการของลูกค้า
            System.out.println("\n===== 4. รายชื่อลูกค้าทั้งหมดและประวัติการส่งซ่อมเครื่อง =====");
            String hql4 = "SELECT c.customerName, r.jobID FROM Customer c LEFT JOIN RepairJob r ON c.customerID = r.customer.customerID";
            List<Object[]> list4 = session.createQuery(hql4).list();
            for (Object[] row : list4) {
                System.out.println("ลูกค้า: " + row[0] + " | รหัสงานซ่อม: " + (row[1] != null ? row[1] : "ยังไม่เคยส่งซ่อม"));
            }

            // 5. รายได้รวมของศูนย์ซ่อม
            System.out.println("\n===== 5. ยอดรายได้รวมทั้งหมดจากใบเสร็จรับเงิน =====");
            String hql5 = "SELECT SUM(rc.totalPrice) FROM Receipt rc";
            Double totalIncome = (Double) session.createQuery(hql5).uniqueResult();
            System.out.println(">>> รายได้รวมสะสมทั้งสิ้น: " + (totalIncome != null ? totalIncome : 0) + " บาท");

            // 6. ตรวจสอบราคาสินค้าคงคลัง
            System.out.println("\n===== 6. ตรวจสอบราคาอะไหล่ชิ้นที่แพงที่สุดในสต็อก =====");
            String hql6 = "SELECT MAX(s.price) FROM SparePart s";
            Double maxPrice = (Double) session.createQuery(hql6).uniqueResult();
            System.out.println("ราคาอะไหล่สูงสุด: " + (maxPrice != null ? maxPrice : 0) + " บาท");

            // 7. จัดลำดับความสำคัญของงาน
            System.out.println("\n===== 7. ตรวจสอบวันนัดหมายคิวซ่อมที่ใกล้ที่สุด =====");
            String hql7 = "SELECT MIN(r.appoinmentDate) FROM RepairJob r WHERE r.status != 'เสร็จสิ้น'";
            Calendar minDate = (Calendar) session.createQuery(hql7).uniqueResult();
            System.out.println("วันนัดหมายเร็วที่สุดคือ: " + (minDate != null ? minDate.getTime() : "ไม่มีงานค้าง"));

            // 8. ข้อมูลติดต่อเพื่อติดตามงาน
            System.out.println("\n===== 8. รายการงานซ่อมปี 2026 และเบอร์โทรศัพท์ลูกค้า =====");
            String hql8 = "SELECT r.jobID, c.customerName, c.customerPhone " +
                          "FROM RepairJob r JOIN r.customer c " +
                          "WHERE function('YEAR', r.dateReceived) = 2026";
            List<Object[]> list8 = session.createQuery(hql8).list();
            for (Object[] row : list8) {
                System.out.println("JobID: " + row[0] + " | ลูกค้า: " + row[1] + " | เบอร์โทรศัพท์: " + row[2]);
            }

            // 9. ค้นหางานตามช่วงเวลา
            System.out.println("\n===== 9. งานซ่อมที่รับเครื่องตั้งแต่วันที่ 1 มีนาคม 2026 เป็นต้นไป =====");
            Calendar filterDate = Calendar.getInstance();
            filterDate.set(2026, Calendar.MARCH, 1);
            String hql9 = "FROM RepairJob r WHERE r.dateReceived >= :startDate";
            List<RepairJob> list9 = session.createQuery(hql9, RepairJob.class)
                                           .setParameter("startDate", filterDate)
                                           .list();
            for (RepairJob r : list9) {
                System.out.println("วันที่รับ: " + r.getDateReceived().getTime() + " | JobID: " + r.getJobID() + " | รุ่น: " + r.getModelName());
            }

            // 10. วิเคราะห์พื้นที่ให้บริการ
            System.out.println("\n===== 10. สรุปจังหวัดที่มีกลุ่มลูกค้าหนาแน่น (มากกว่า 1 คน) =====");
            String hql10 = "SELECT c.address, COUNT(c.customerID) FROM Customer c GROUP BY c.address HAVING COUNT(c.customerID) > 1";
            List<Object[]> list10 = session.createQuery(hql10).list();
            for (Object[] row : list10) {
                System.out.println("จังหวัด: " + row[0] + " | จำนวนลูกค้าปัจจุบัน: " + row[1] + " ราย");
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
