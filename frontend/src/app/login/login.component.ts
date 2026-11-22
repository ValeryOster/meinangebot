import { Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { AuthService } from '../service/security/auth.service';
import { TokenStorageService } from '../service/security/token-storage.service';
import Swal from 'sweetalert2';

import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  // Angular 22 nutzt standardmäßig OnPush; der bestehende Code setzt auf die bisherige Prüfung
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  singupForm: FormGroup;
  container = 'container';
  singInCssClass: boolean = true;
  newAccountCssClass: boolean = false;
  passwortCssClass: boolean = false;
  showLoginPassword = false;
  showSignupPassword = false;

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router,
    private token: TokenStorageService,
  ) {}

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      loginUsername: [''],
      loginPassword: [''],
    });
    this.singupForm = this.formBuilder.group({
      signupUsername: [''],
      signupEmail: [''],
      signupPassword: [''],
    });
  }

  get loginF() {
    return this.loginForm.controls;
  }

  get singupF() {
    return this.singupForm.controls;
  }

  login() {
    this.authService
      .login({
        username: this.loginF.loginUsername.value,
        password: this.loginF.loginPassword.value,
      })
      .subscribe(
        (success) => {
          if (success) {
            this.token.saveToken(success.token);
            this.token.saveUser(success);
            this.token.saveRoles(success.roles);
            this.router.navigate(['/']);
          }
        },
        (error) => {
          Swal.fire('Error', this.getErrorMessage(error), 'error');
        },
      );
  }

  signup() {
    this.authService
      .signup({
        username: this.singupF.signupUsername.value,
        email: this.singupF.signupEmail.value,
        password: this.singupF.signupPassword.value,
      })
      .subscribe(
        (success) => {
          if (success) {
            Swal.fire('Success', success.message.replace('Success:', ' '), 'success');
            this.router.navigate(['']);
          }
        },
        (error) => {
          Swal.fire('Error', this.getErrorMessage(error), 'error');
        },
      );
  }

  private getErrorMessage(error): string {
    const message =
      error && error.error && error.error.message
        ? error.error.message
        : error && error.error && error.error.error
          ? error.error.error
          : 'Request failed';

    return String(message).replace('Error:', ' ').trim();
  }

  /** Passwort-Zurücksetzen gibt es im Backend noch nicht – Hinweis statt Seiten-Neuladen. */
  resetPassword(event: Event) {
    event.preventDefault();
    Swal.fire('Info', 'Das Zurücksetzen des Passworts ist noch nicht verfügbar. Bitte wende dich an den Administrator.', 'info');
  }

  changeView(str: string) {
    if (str === 'singIn') {
      this.singInCssClass = true;
      this.newAccountCssClass = false;
      this.passwortCssClass = false;
    } else if (str === 'newAccount') {
      this.singInCssClass = false;
      this.newAccountCssClass = true;
      this.passwortCssClass = false;
    } else if (str === 'vorgotPWD') {
      this.singInCssClass = false;
      this.newAccountCssClass = false;
      this.passwortCssClass = true;
    }
  }
}
