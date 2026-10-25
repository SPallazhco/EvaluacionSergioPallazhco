import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('muestra el nombre del sistema en la barra superior', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();

    const titulo = (fixture.nativeElement as HTMLElement).querySelector('h1');
    expect(titulo?.textContent).toContain('Transferencias Interbancarias');
  });
});
