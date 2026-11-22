import {Component, OnDestroy, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {Subscription, timer} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {Discounter, DiscounterService, GatherStartResponse, GatherStatus} from '../../service/server/discounter.service';
import {DecimalPipe, KeyValuePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';

interface DiscounterChoice {
  name: string;
  selected: boolean;
}

@Component({
  selector: 'app-gathering',
  // Angular 22 nutzt standardmäßig OnPush; der bestehende Code setzt auf die bisherige Prüfung
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [DecimalPipe, KeyValuePipe, FormsModule],
  templateUrl: './gathering.component.html',
  styleUrls: ['./gathering.component.css']
})
export class GatheringComponent implements OnInit, OnDestroy {

  /** Edeka ist deaktiviert (API liefert HTTP 403) und wird deshalb nicht angeboten. */
  choices: DiscounterChoice[] = [
    {name: 'PENNY', selected: false},
    {name: 'ALDI', selected: false},
    {name: 'NETTO', selected: false}
  ];
  confirmed = false;
  reports: Discounter[] = [];
  status: GatherStatus;
  message: string;
  error: string;

  private polling: Subscription;

  constructor(private discService: DiscounterService) {
  }

  ngOnInit(): void {
    this.loadReports();
    this.refreshStatus();
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  get canStart(): boolean {
    return this.confirmed && !this.status?.running && this.choices.some(c => c.selected);
  }

  submit(): void {
    const selected = this.choices.filter(c => c.selected).map(c => c.name);
    this.message = null;
    this.error = null;
    this.discService.startGather(selected).subscribe(
      (response: GatherStartResponse) => {
        this.message = response.message;
        this.confirmed = false;
        this.startPolling();
      },
      (err: HttpErrorResponse) => {
        this.error = err.error?.message || `Fehler ${err.status}: ${err.statusText}`;
        this.refreshStatus();
      });
  }

  /** Neueste Berichte zuerst. */
  get sortedReports(): Discounter[] {
    return [...this.reports].sort((a, b) => (b.id || 0) - (a.id || 0));
  }

  isError(result: string): boolean {
    return result != null && result.startsWith('FEHLER');
  }

  private loadReports(): void {
    this.discService.getAll().subscribe(value => this.reports = value || []);
  }

  private refreshStatus(): void {
    this.discService.getStatus().subscribe(status => {
      this.status = status;
      if (status.running) {
        this.startPolling();
      }
    });
  }

  private startPolling(): void {
    if (this.polling) {
      return;
    }
    this.polling = timer(1000, 3000)
      .pipe(switchMap(() => this.discService.getStatus()))
      .subscribe(status => {
        this.status = status;
        if (!status.running) {
          this.stopPolling();
          this.message = 'Sammeln beendet.';
          this.loadReports();
        }
      });
  }

  private stopPolling(): void {
    if (this.polling) {
      this.polling.unsubscribe();
      this.polling = null;
    }
  }
}
