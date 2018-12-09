package file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class FileUtils {

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		createZipForStudentData();
	}

	private static void createZipForStudentData() throws IOException, JsonGenerationException, JsonMappingException {
		List<Student> students = prepareData();
		List<File> files = new ArrayList<>();
		String basePath = "E:/workspace/java-practice/Practice/Files";
		System.out.println(basePath.substring(0, basePath.lastIndexOf("/")));
		for(Student student : students) {
			String path = basePath + File.separator + student.getName();
			String fileName = path + File.separator + student.getName() + ".json";
			files.add(createFileFromContent(getJsonStringFromObject(student), fileName));
			createFilesForCourses(files, path, student.getCourses(), student.getName());
		}
		System.out.println(files.size());
		System.out.println("Complete");
		pack(basePath, basePath.substring(0, basePath.lastIndexOf("/") + 1) + "test.zip");
	}
	
	public static void pack(String sourceDirPath, String zipFilePath) throws IOException {
	    Path p = Files.createFile(Paths.get(zipFilePath));
	    Path pp = Paths.get(sourceDirPath);
	    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p));
	        Stream<Path> paths = Files.walk(pp)) {
	        paths
	          .filter(path -> !Files.isDirectory(path))
	          .forEach(path -> {
	              ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
	              try {
	                  zs.putNextEntry(zipEntry);
	                  Files.copy(path, zs);
	                  zs.closeEntry();
	            } catch (IOException e) {
	                System.err.println(e);
	            }
	          });
	    }
	}
	
	private static void createZipFromFiles(String basePath, List<File> files) throws IOException {
		try {
			File f = new File(basePath.substring(0, basePath.lastIndexOf("/") + 1) + "test.zip");
			System.out.println(f.getAbsolutePath());
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
			for(File file : files) {
				ZipEntry e = new ZipEntry(file.getName());
				out.putNextEntry(e);
				out.write(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
				out.closeEntry();
			}
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			org.apache.commons.io.FileUtils.deleteDirectory(new File(basePath));
		}
	}
	
	private static void createFilesForCourses(List<File> files, String basePath, List<Course> courses, String string) throws JsonGenerationException, JsonMappingException, IOException {
		for(Course course : courses) {
			String path = basePath + File.separator + course.getCourceName();
			String fileName = path + File.separator + string + "_" + course.getCourceName() + ".json";
			files.add(createFileFromContent(getJsonStringFromObject(course), fileName));
		}
	}

	public static String getJsonStringFromObject(Object object) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		return mapper.writeValueAsString(object);
	}
	
	public static File createFileFromContent(String fileContent, String fileName) throws IOException {
		File file = new File(fileName);
		if(!file.exists() && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(fileContent.getBytes());
		} catch(IOException e) {
			e.printStackTrace();
		}
		return file;
	} 
	
	private static List<Student> prepareData() {
		
		List<Course> courses = new ArrayList<>();
		
		Course physics = new Course("Physics", 96);
		Course chemistry = new Course("Chemistry", 87);
		Course maths = new Course("Maths", 91);
		
		courses.add(physics);
		courses.add(chemistry);
		courses.add(maths);
		
		List<Student> students = new ArrayList<>();
		
		Student s1 = new Student();
		s1.setAge(17);
		s1.setCourses(courses);
		s1.setMobileNumber(9586809367l);
		s1.setName("Harsh");
		s1.setStandard(12);
		
		Student s2 = new Student();
		s2.setAge(17);
		s2.setCourses(courses);
		s2.setMobileNumber(9588807367l);
		s2.setName("Dhruv");
		s2.setStandard(12);
		
		students.add(s1);
		students.add(s2);
		
		return students;
	}
}

class Student {
	
	private String name;
	private Integer age;
	private List<Course> courses;
	private Long mobileNumber;
	private Integer standard;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public List<Course> getCourses() {
		return courses;
	}
	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}
	public Long getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(Long mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public Integer getStandard() {
		return standard;
	}
	public void setStandard(Integer standard) {
		this.standard = standard;
	}
}

class Course {
	
	private String courceName;
	private Integer mark;
	
	public Course(String courceName, Integer mark) {
		this.courceName = courceName;
		this.mark = mark;
	}
	
	public String getCourceName() {
		return courceName;
	}
	public void setCourceName(String courceName) {
		this.courceName = courceName;
	}
	public Integer getMark() {
		return mark;
	}
	public void setMark(Integer mark) {
		this.mark = mark;
	}
}