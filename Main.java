import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

class Student {
    private String nume;
    private String prenume;
    private String email;
    private LocalDate dataNasterii;
    private boolean absolvit;

    public Student(String nume, String prenume, String email, LocalDate dataNasterii) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.dataNasterii = dataNasterii;
        this.absolvit = false;
    }

    public String getNume() {
        return nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDataNasterii() {
        return dataNasterii;
    }

    public boolean isAbsolvit() {
        return absolvit;
    }

    public void setAbsolvit(boolean absolvit) {
        this.absolvit = absolvit;
    }

    public String toFileLine(String numeFacultate) {
        return nume + "," + prenume + "," + email + "," + dataNasterii + "," + absolvit + "," + numeFacultate;
    }
}

class Facultate {
    private String nume;
    String abreviere;
    String domeniu;
    private List<Student> studenti;

    public Facultate(String nume, String abreviere, String domeniu) {
        this.nume = nume;
        this.abreviere = abreviere;
        this.domeniu = domeniu;
        this.studenti = new ArrayList<>();
    }

    public void adaugaStudent(Student student) {
        studenti.add(student);
    }

    public List<Student> getStudenti() {
        return studenti;
    }

    public void afiseazaStudenti() {
        for (Student student : studenti) {
            System.out.println(student.getNume() + " " + student.getPrenume() + " - " + student.getEmail());
        }
    }

    public String getNume() {
        return nume;
    }

    public void salveazaStudenti(String numeFisier, String numeFacultate) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(numeFisier, true))) {
            for (Student student : studenti) {
                writer.write(student.toFileLine(numeFacultate));
                writer.newLine();
            }
        }
    }
}

class SistemManagementStudenti {
    private Map<String, Facultate> facultati;
    private List<Student> studentiNeatribuiti;
    private BufferedWriter logWriter;

    public SistemManagementStudenti() {
        this.facultati = new HashMap<>();
        this.studentiNeatribuiti = new ArrayList<>();
        try {
            incarcaFacultati("facultati.txt");
            incarcaStudenti("studenti.txt");
            this.logWriter = new BufferedWriter(new FileWriter("log_operatii.txt", true));
        } catch (IOException e) {
            System.out.println("Eroare la deschiderea fisierului de log: " + e.getMessage());
        }
    }

    private void incarcaFacultati(String numeFisier) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(numeFisier))) {
            String linie;
            while ((linie = reader.readLine()) != null) {
                String[] parts = linie.split(",");
                if (parts.length >= 3) {
                    String numeFacultate = parts[0];
                    String abreviereFacultate = parts[1];
                    String domeniuFacultate = parts[2];
                    facultati.put(numeFacultate, new Facultate(numeFacultate, abreviereFacultate, domeniuFacultate));
                }
            }
        }
    }

    private void incarcaStudenti(String numeFisier) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(numeFisier))) {
            String linie;
            while ((linie = reader.readLine()) != null) {
                String[] parts = linie.split(",");
                if (parts.length >= 6) {
                    String nume = parts[0];
                    String prenume = parts[1];
                    String email = parts[2];
                    LocalDate dataNasterii = LocalDate.parse(parts[3]);
                    boolean absolvit = Boolean.parseBoolean(parts[4]);
                    String numeFacultate = parts[5];
                    Student student = new Student(nume, prenume, email, dataNasterii);
                    student.setAbsolvit(absolvit);
                    if (facultati.containsKey(numeFacultate)) {
                        facultati.get(numeFacultate).adaugaStudent(student);
                    } else {
                        studentiNeatribuiti.add(student);
                    }
                } else {
                    System.out.println("Linie incorecta in fisierul studenti.txt: " + linie);
                }
            }
        }
    }

    public void creeazaFacultate(String nume, String abreviere, String domeniu) {
        facultati.put(nume, new Facultate(nume, abreviere, domeniu));
        try {
            salveazaFacultati("facultati.txt");
            inregistreazaOperatie("Creata facultatea " + nume);
        } catch (IOException e) {
            System.out.println("Eroare la salvarea facultatii: " + e.getMessage());
        }
    }

    public void adaugaStudentLaFacultate(String numeFacultate, Student student) {
        Facultate facultate = facultati.get(numeFacultate);
        if (facultate != null) {
            facultate.adaugaStudent(student);
            try {
                facultate.salveazaStudenti("studenti.txt", numeFacultate);
            } catch (IOException e) {
                System.out.println("Eroare la salvarea studentului: " + e.getMessage());
            }
            inregistreazaOperatie("Atribuit studentul " + student.getNume() + " la facultatea " + numeFacultate);
        }
    }

    public void marcheazaAbsolvireStudent(String numeFacultate, String numeStudent) {
        if (facultati.containsKey(numeFacultate)) {
            Facultate facultate = facultati.get(numeFacultate);
            List<Student> studenti = facultate.getStudenti();
            for (Student student : studenti) {
                if (student.getNume().equals(numeStudent)) {
                    student.setAbsolvit(true);
                    try {
                        facultate.salveazaStudenti("studenti.txt", numeFacultate); 
                    } catch (IOException e) {
                        System.out.println("Eroare la salvarea datelor studentului: " + e.getMessage());
                    }
                    inregistreazaOperatie("Studentul " + numeStudent + " a absolvit la facultatea " + numeFacultate);
                    return;
                }
            }
            System.out.println("Studentul nu a fost găsit în facultatea specificată.");
        } else {
            System.out.println("Facultatea nu există.");
        }
    }

    public void afiseazaFacultati() {
        System.out.println("Facultati:");
        for (String numeFacultate : facultati.keySet()) {
            System.out.println(numeFacultate);
        }
    }

    public void afiseazaStudentiFacultate(String numeFacultate) {
        if (facultati.containsKey(numeFacultate)) {
            Facultate facultate = facultati.get(numeFacultate);
            facultate.afiseazaStudenti();
        } else {
            System.out.println("Facultatea nu există.");
        }
        System.out.println("Studentii neatribuiti:");
        for (Student student : studentiNeatribuiti) {
            System.out.println(student.getNume() + " " + student.getPrenume() + " - " + student.getEmail());
        }
    }

    public void inregistreazaOperatie(String operatie) {
        try {
            logWriter.write(LocalDateTime.now() + " - " + operatie);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.out.println("Eroare la scrierea in fisierul de log: " + e.getMessage());
        }
    }

    public void inchideLog() {
        try {
            logWriter.close();
        } catch (IOException e) {
            System.out.println("Eroare la inchiderea fisierului de log: " + e.getMessage());
        }
    }

    public void salveazaFacultati(String numeFisier) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(numeFisier))) {
            for (Facultate facultate : facultati.values()) {
                writer.write(facultate.getNume() + "," + facultate.abreviere + "," + facultate.domeniu);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) {
        SistemManagementStudenti sistem = new SistemManagementStudenti();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Selectati o operatie:");
            System.out.println("1. Creeaza o noua facultate");
            System.out.println("2. Atribuie un student la o facultate");
            System.out.println("3. Afiseaza toate facultatile");
            System.out.println("4. Afiseaza studentii unei facultati si studentii neatribuiti");
            System.out.println("5. Marcheaza absolvire student");
            System.out.println("0. Iesire");

            int alegere = scanner.nextInt();
            scanner.nextLine();

            switch (alegere) {
                case 1:
                    System.out.println("Introduceti numele facultatii:");
                    String numeFacultate = scanner.nextLine();
                    System.out.println("Introduceti abrevierea facultatii:");
                    String abreviereFacultate = scanner.nextLine();
                    System.out.println("Introduceti domeniu facultatii:");
                    String domeniuFacultate = scanner.nextLine();
                    sistem.creeazaFacultate(numeFacultate, abreviereFacultate, domeniuFacultate);
                    break;
                case 2:
                    System.out.println("Introduceti numele facultatii:");
                    String facultate = scanner.nextLine();
                    System.out.println("Introduceti numele studentului:");
                    String numeStudent = scanner.nextLine();
                    System.out.println("Introduceti prenumele studentului:");
                    String prenumeStudent = scanner.nextLine();
                    System.out.println("Introduceti email-ul studentului:");
                    String emailStudent = scanner.nextLine();
                    System.out.println("Introduceti data nasterii a studentului (YYYY-MM-DD):");
                    LocalDate dataNasterii = LocalDate.parse(scanner.nextLine());
                    sistem.adaugaStudentLaFacultate(facultate, new Student(numeStudent, prenumeStudent, emailStudent, dataNasterii));
                    break;
                case 3:
                    sistem.afiseazaFacultati();
                    break;
                case 4:
                    System.out.println("Introduceti numele facultatii:");
                    String facultateAfisare = scanner.nextLine();
                    sistem.afiseazaStudentiFacultate(facultateAfisare);
                    break;
                case 5:
                    System.out.println("Introduceti numele facultatii:");
                    String facultateAbsolvire = scanner.nextLine();
                    System.out.println("Introduceti numele studentului care absolveste:");
                    String numeStudentAbsolvire = scanner.nextLine();
                    sistem.marcheazaAbsolvireStudent(facultateAbsolvire, numeStudentAbsolvire);
                    break;
                case 0:
                    sistem.inchideLog();
                    scanner.close();
                    return;
                default:
                    System.out.println("Alegere invalida. Va rugam introduceti un numar intre 0 si 5.");
            }
        }
    }
}
