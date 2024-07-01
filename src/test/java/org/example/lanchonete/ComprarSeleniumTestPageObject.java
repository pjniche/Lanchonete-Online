package org.example.lanchonete;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ComprarSeleniumTestPageObject {
    private WebDriver driver;

    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        driver.get("http://localhost:8080/Gradle___org_example___LanchoneteOnline_1_0_SNAPSHOT_war/view/home/home.html");
    }

    public void tearDown() {
        driver.quit();
    }

    public void realizarClickHome() {
        driver.findElement(By.xpath("/html/body/div/div[2]/div/div[3]/button")).click();
    }

    public void realizarClickLanche() {
        driver.findElement(By.xpath("/html/body/div/div[1]/div[2]/a")).click();
        driver.findElement(By.xpath("/html/body/div/div[2]/div[1]/div[1]/div/div[2]/div/div[2]/button[2]")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    public void realizarClickBebida() {
        driver.findElement(By.xpath("/html/body/div/div[1]/div[3]/a")).click();
        driver.findElement(By.xpath("/html/body/div/div[2]/div[2]/div[1]/div/div[2]/div/div[2]/button[2]")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    public void realizarClickCarrinho() {
        driver.findElement(By.xpath("/html/body/div/div[1]/div[6]/a")).click();
    }

    public void loginUsuario() {
        driver.findElement(By.id("loginInput")).sendKeys("pjniche");
        driver.findElement(By.id("senhaInput")).sendKeys("123456");
        driver.findElement(By.className("buttonSubmit")).click();
    }

    public void pedidoCheckout() {
        driver.findElement(By.xpath("/html/body/div/div/div[1]/div[4]/input[2]")).click();
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div[4]/div[2]/div[2]/input")).click();
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div[4]/button")).click();
    }

    public void adicionarTempoDeEspera(int tempo) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(tempo));
    }
}
