import {Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {ManageComponent} from './manage/manage.component';
import {authGuard} from './service/security/auth.guard';
import {LoginComponent} from './login/login.component';
import {AuswahlComponent} from './auswahl/auswahl.component';

export const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'auswahl', component: AuswahlComponent},
  {path: 'manage', component: ManageComponent, canActivate: [authGuard], data: {role: 'ROLE_ADMIN'}},
  {path: 'login', component: LoginComponent}
];
