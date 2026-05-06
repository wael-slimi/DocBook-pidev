package org.docbook;

import org.docbook.entities.users.User;
import org.docbook.entities.users.Doctor;
import org.docbook.services.users.UserService;
import org.docbook.services.users.DoctorService;
import java.util.Scanner;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserService userService = new UserService();
        DoctorService doctorService = new DoctorService();
        boolean exitApp = false;

        while (!exitApp) {
            System.out.println("\n--- DOCBOOK MAIN MENU ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            String mainChoice = sc.nextLine();

            switch (mainChoice) {
                case "1":
                    handleRegistration(sc, userService);
                    break;
                case "2":
                    User user = handleLogin(sc, userService);
                    if (user != null) {
                        handleDashboard(sc, user, userService, doctorService);
                    }
                    break;
                case "3":
                    exitApp = true;
                    break;
                default:
                    System.out.println("Invalid input.");
            }
        }
        sc.close();
    }

    private static void handleDashboard(Scanner sc, User user, UserService us, DoctorService ds) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- DASHBOARD (" + user.getDtype().toUpperCase() + ") ---");
            System.out.println("1. My Profile (Manage Account)");
            System.out.println("2. Doctor Directory");
            System.out.println("3. Logout");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    if (user.getDtype().equalsIgnoreCase("doctor")) {
                        handleDoctorCRUD(sc, (Doctor) ds.getDoctorFullProfile(user.getId()), ds, us);
                    } else {
                        handlePatientCRUD(sc, user, us);
                    }
                    break;
                case "2":
                    handleDoctorSearch(sc, ds);
                    break;
                case "3":
                    loggedIn = false;
                    break;
            }
        }
    }

    private static void handleDoctorSearch(Scanner sc, DoctorService ds) {
        boolean searching = true;
        while (searching) {
            System.out.println("\n--- DOCTOR DIRECTORY ---");
            System.out.println("1. List All Doctors");
            System.out.println("2. Search by Name");
            System.out.println("3. Back to Dashboard");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1") || choice.equals("2")) {
                String searchName = "";
                if (choice.equals("2")) {
                    System.out.print("Enter doctor name: ");
                    searchName = sc.nextLine();
                }

                List<Doctor> results = ds.searchDoctors(searchName);

                if (results.isEmpty()) {
                    System.out.println("No doctors found.");
                } else {
                    System.out.println("\n--- RESULTS FOUND ---");
                    for (Doctor d : results) {
                        displayDoctorCard(d);
                    }
                }
            } else if (choice.equals("3")) {
                searching = false;
            }
        }
    }

    // Helper method to format the doctor info neatly
    private static void displayDoctorCard(Doctor d) {
        System.out.println("-------------------------------------------");
        System.out.println("NAME:      Dr. " + d.getName());
        System.out.println("SPECIALTY: " + (d.getSpecialty() != null ? d.getSpecialty() : "Generalist"));
        System.out.println("FEE:       " + d.getConsultationFee() + " DT");
        System.out.println("BIO:       " + (d.getBio() != null ? d.getBio() : "No bio available."));
        System.out.println("-------------------------------------------");
    }

    private static void handlePatientCRUD(Scanner sc, User user, UserService us) {
        boolean managing = true;
        while (managing) {
            System.out.println("\n--- PATIENT PROFILE ---");
            System.out.println("1. Update Profile (Name/Email)");
            System.out.println("2. Delete My Account");
            System.out.println("3. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                System.out.println("Leave blank to keep current value.");
                System.out.print("New Name: ");
                String n = sc.nextLine();
                System.out.print("New Email: ");
                String e = sc.nextLine();

                User updateData = new User();
                updateData.setId(user.getId());
                updateData.setName(n);
                updateData.setEmail(e);

                us.update(updateData);

                User refreshed = us.read(user.getId());
                user.setName(refreshed.getName());
                user.setEmail(refreshed.getEmail());
            }
            else if (choice.equals("2")) {
                System.out.print("Permanently delete account? (y/n): ");
                if (sc.nextLine().equalsIgnoreCase("y")) {
                    us.delete(user.getId());
                    System.out.println("Goodbye.");
                    System.exit(0);
                }
            }
            else if (choice.equals("3")) {
                managing = false;
            }
        }
    }

    private static void handleDoctorCRUD(Scanner sc, Doctor doc, DoctorService ds, UserService us) {
        boolean managing = true;
        while (managing) {
            System.out.println("\n--- MANAGE DOCTOR ACCOUNT ---");
            System.out.println("1. Update Base Info (Name/Email)");
            System.out.println("2. Update Medical Info (Specialty/Fee/Bio)");
            System.out.println("3. Delete Account");
            System.out.println("4. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                System.out.print("New Name: "); doc.setName(sc.nextLine());
                System.out.print("New Email: "); doc.setEmail(sc.nextLine());
                us.update(doc);
            } else if (choice.equals("2")) {
                System.out.print("Specialty: "); doc.setSpecialty(sc.nextLine());
                System.out.print("Fee: "); doc.setConsultationFee(Double.parseDouble(sc.nextLine()));
                System.out.print("Bio: "); doc.setBio(sc.nextLine());
                ds.updateDoctorProfile(doc);
            } else if (choice.equals("3")) {
                System.out.print("Confirm deletion? (y/n): ");
                if (sc.nextLine().equalsIgnoreCase("y")) {
                    us.delete(doc.getId());
                    System.exit(0);
                }
            } else if (choice.equals("4")) {
                managing = false;
            }
        }
    }

    private static void handleRegistration(Scanner sc, UserService us) {
        System.out.print("Name: "); String n = sc.nextLine();
        System.out.print("Email: "); String e = sc.nextLine();
        System.out.print("Pass: "); String p = sc.nextLine();
        System.out.print("Type (doctor/patient): "); String t = sc.nextLine().toLowerCase();
        us.create(new User(n, e, p, t, t));
    }

    private static User handleLogin(Scanner sc, UserService us) {
        System.out.print("Email: "); String e = sc.nextLine();
        System.out.print("Pass: "); String p = sc.nextLine();
        return us.login(e, p);
    }
}