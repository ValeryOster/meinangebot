import { Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';
import {GatheringComponent} from './gathering/gathering.component';
import {DellastitmesComponent} from './dellastitmes/dellastitmes.component';

@Component({
  selector: 'app-manage',
  // Angular 22 nutzt standardmäßig OnPush; der bestehende Code setzt auf die bisherige Prüfung
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [MatTabsModule, GatheringComponent, DellastitmesComponent],
  templateUrl: './manage.component.html',
  styleUrls: ['./manage.component.css']
})
export class ManageComponent implements OnInit {

  ngOnInit(): void {
  }

}
