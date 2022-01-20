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

  constructor(private authService: AuthService, private formBuilder: FormBuilder,
              private router: Router, private token: TokenStorageService,private offers:OffersComponent) {
  }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      username: [''],
      password: ['']
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  login() {
    this.authService.login(
      {
        username: this.f.username.value,
        password: this.f.password.value
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

    if (str === 'singUp') {
      this.container = "container active";
      console.log("singUp")
    }else {
      this.container = "container";
      console.log("login")
    }
  }
}
