import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from "../service/security/auth.service";
import {TokenStorageService} from "../service/security/token-storage.service";
import {OffersComponent} from "../offers/offers.component";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  container = "container";
  singInCssClass: boolean = true;
  newAccountCssClass: boolean = false;
  passwortCssClass: boolean = false;

  constructor(private authService: AuthService, private formBuilder: FormBuilder,
              private router: Router, private token: TokenStorageService,private offers:OffersComponent) {
  }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      signipUsername: [''],
      signinPassword: ['']
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  login() {
    console.log(this.loginForm)
    this.authService.login(
      {
        username: this.f.signipUsername.value,
        password: this.f.signinPassword.value
      }
    ).subscribe(success => {
      if (success) {
        console.log("is success!")
        this.token.saveToken(success.token);
        this.token.saveUser(success);
        this.router.navigate(['/']);
      }
    });
  }

  changeView(str: string) {

    if (str === 'singIn') {
      this.singInCssClass = true;
      this.newAccountCssClass = false;
      this.passwortCssClass = false;
    }else if(str === 'newAccount'){
      this.singInCssClass = false;
      this.newAccountCssClass = true;
      this.passwortCssClass = false;
    }else if (str === 'vorgotPWD') {
      this.singInCssClass = false;
      this.newAccountCssClass = false;
      this.passwortCssClass = true;
    }
  }
}
