package com.lab.Project_JAVA;

import java.util.List;
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

			// 1 หางานซ่อมที่ใช้อะไหล่มากที่สุด
			System.out.println("\n========== ข้อ 1 งานซ่อมที่ใช้อะไหล่มากที่สุด ==========\n");

			String hql1 = "SELECT r.jobID, SUM(q.amount) " + "FROM Quotation q " + "JOIN q.repairJob r "
					+ "GROUP BY r.jobID " + "ORDER BY SUM(q.amount) DESC";

			Query<Object[]> query1 = session.createQuery(hql1, Object[].class);
			List<Object[]> rows1 = query1.getResultList();

			for (Object[] row : rows1) {
				System.out.println("JobID: " + row[0] + " PartsUsed: " + row[1]);
			}

			// 2 หาอะไหล่ที่ราคาสูงกว่าค่าเฉลี่ย
			System.out.println("\n========== ข้อ 2 อะไหล่ที่ราคาสูงกว่าค่าเฉลี่ย ==========\n");

			String hql2 = "FROM SparePart s " + "WHERE s.price > (SELECT AVG(p.price) FROM SparePart p)";

			Query<SparePart> query2 = session.createQuery(hql2, SparePart.class);
			List<SparePart> rows2 = query2.getResultList();

			for (SparePart s : rows2) {
				System.out.println("PartName: " + s.getPartName() + " Price: " + s.getPrice());
			}

			// 3 แสดงสถานะงานด้วย CASE
			System.out.println("\n========== ข้อ 3 แสดงสถานะงานแบบ CASE ==========\n");

			String hql3 = "SELECT r.jobID, " + "CASE " + "WHEN r.status = 'เสร็จสิ้น' THEN 'Complete' "
					+ "WHEN r.status = 'กำลังซ่อม' THEN 'Working' " + "ELSE 'Waiting' " + "END " + "FROM RepairJob r";

			Query<Object[]> query3 = session.createQuery(hql3, Object[].class);
			List<Object[]> rows3 = query3.getResultList();

			for (Object[] row : rows3) {
				System.out.println("JobID: " + row[0] + " Status: " + row[1]);
			}

			// 4 หาลูกค้าที่มีงานซ่อมมากที่สุด
			System.out.println("\n========== ข้อ 4 ลูกค้าที่มีงานซ่อมมากที่สุด ==========\n");

			String hql4 = "SELECT c.customerName, COUNT(r.jobID) " + "FROM RepairJob r " + "JOIN r.customer c "
					+ "GROUP BY c.customerName " + "ORDER BY COUNT(r.jobID) DESC";

			Query<Object[]> query4 = session.createQuery(hql4, Object[].class);
			List<Object[]> rows4 = query4.getResultList();

			for (Object[] row : rows4) {
				System.out.println("Customer: " + row[0] + " Jobs: " + row[1]);
			}

			// 5 หางานซ่อมที่ยังไม่มีรีวิว
			System.out.println("\n========== ข้อ 5 งานซ่อมที่ยังไม่มีรีวิว ==========\n");

			String hql5 = "FROM RepairJob r " + "WHERE NOT EXISTS "
					+ "(SELECT rv.ReviewID FROM Review rv WHERE rv.repairJob = r)";

			Query<RepairJob> query5 = session.createQuery(hql5, RepairJob.class);
			List<RepairJob> rows5 = query5.getResultList();

			for (RepairJob r : rows5) {
				System.out.println("JobID: " + r.getJobID());
			}

			// 6 หาอะไหล่ที่ถูกใช้งาน
			System.out.println("\n========== ข้อ 6 อะไหล่ที่ถูกใช้งาน ==========\n");

			String hql6 = "SELECT DISTINCT s.partName " + "FROM Quotation q " + "JOIN q.sparePart s";

			Query<String> query6 = session.createQuery(hql6, String.class);
			List<String> rows6 = query6.getResultList();

			for (String name : rows6) {
				System.out.println("Used Part: " + name);
			}

			// 7 แสดงระดับค่าอะไหล่ของงานซ่อม
			System.out.println("\n========== ข้อ 7 ระดับค่าอะไหล่ของงานซ่อม ==========\n");

			String hql7 = "SELECT r.jobID, "
			        + "SUM(s.price * q.amount), "
			        + "CASE "
			        + "WHEN SUM(s.price * q.amount) >= 5000 THEN 'High Cost' "
			        + "WHEN SUM(s.price * q.amount) >= 2000 THEN 'Medium Cost' "
			        + "ELSE 'Low Cost' "
			        + "END "
			        + "FROM Quotation q "
			        + "JOIN q.repairJob r "
			        + "JOIN q.sparePart s "
			        + "GROUP BY r.jobID";

			Query<Object[]> query7 = session.createQuery(hql7, Object[].class);
			List<Object[]> rows7 = query7.getResultList();

			for (Object[] row : rows7) {
			    System.out.println("JobID: " + row[0] + " TotalCost: " + row[1] + " Level: " + row[2]);
			}
			
			// 8 ช่างที่เสนอใบเสนอราคามากกว่า 1 ครั้ง
			System.out.println("\n========== ข้อ 8 ช่างที่เสนอใบเสนอราคามากกว่า 1 ==========\n");

			String hql8 = "SELECT t, COUNT(q) "
			        + "FROM Technician t "
			        + "LEFT JOIN Quotation q ON q.technician = t "
			        + "GROUP BY t "
			        + "HAVING COUNT(q) > 1";

			Query<Object[]> query8 = session.createQuery(hql8, Object[].class);
			List<Object[]> rows8 = query8.getResultList();

			for (Object[] row : rows8) {
			    Technician t = (Technician) row[0];
			    System.out.println("Technician: " + t.getTechName() + " Quotations: " + row[1]);
			}
			
			// 9 งานซ่อมที่มีรีวิวคะแนนสูงสุด
			System.out.println("\n========== ข้อ 9 งานซ่อมที่มีรีวิวคะแนนสูงสุด ==========\n");

			String hql9 = "SELECT r.jobID, rv.rating "
			        + "FROM Review rv "
			        + "JOIN rv.repairJob r "
			        + "WHERE rv.rating = (SELECT MAX(r2.rating) FROM Review r2)";
			
			Query<Object[]> query9 = session.createQuery(hql9, Object[].class);
			List<Object[]> rows9 = query9.getResultList();

			for (Object[] row : rows9) {
			    System.out.println("JobID: " + row[0] + " Rating: " + row[1]);
			}

			// 10 จำนวนอะไหล่ทั้งหมดที่ใช้ในแต่ละงานซ่อม
			System.out.println("\n========== ข้อ 10 จำนวนอะไหล่ในแต่ละงาน ==========\n");

			String hql10 = "SELECT r.jobID, SUM(q.amount) "
			        + "FROM RepairJob r "
			        + "LEFT JOIN Quotation q ON q.repairJob = r "
			        + "GROUP BY r.jobID";

			Query<Object[]> query10 = session.createQuery(hql10, Object[].class);
			List<Object[]> rows10 = query10.getResultList();

			for (Object[] row : rows10) {
			    System.out.println("JobID: " + row[0] + " TotalParts: " + row[1]);
			}

			tx.commit();
			session.close();

			System.out.println("\n========== Query Completed ==========");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
